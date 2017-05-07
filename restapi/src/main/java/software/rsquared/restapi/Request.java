package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import software.rsquared.restapi.exceptions.InitialRequirementsException;
import software.rsquared.restapi.exceptions.RefreshTokenException;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.exceptions.UserServiceNotInitialized;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonSerializer;
import software.rsquared.restapi.serialization.Serializer;

/**
 * Base implementation of the Request methods
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class Request<T> {

    public static final MediaType MULTIPART_FORM_DATA = MediaType.parse("multipart/form-data");
    public static final MediaType APPLICATION_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");
    public static final MediaType APPLICATION_JSON = MediaType.parse("application/json");

    protected static final String ACCESS_TOKEN = "access_token";
    protected static final String AUTHORIZATION = "BasicAuthorization";
    protected static final String CONTENT_TYPE = "Content-Type";

    private static final ThreadLock LOCK = new ThreadLock();

    protected final OkHttpClient httpClient;
    private final RequestExecutor executor;
    private final List<Parameter> bodyParameters = new ArrayList<>();
    private final List<Parameter> urlParameters = new ArrayList<>();
    private final Map<String, String> headerMap = new HashMap<>();
    private final Timer timer = new Timer();
    private MediaType mediaType = APPLICATION_URLENCODED;
    private String[] urlSegments;
    private String url;
    private long minExecutionTime;
    private boolean authorizedRequest;
    private RestAuthorizationService userService;
    private boolean ignoreErrorCallback;
    private boolean disableLogging;

    /**
     * Initial constructor for request. This constructor creates http client and prepare executor
     */
    protected Request() {
        if (!RestApi.isConfigured()) {
            throw new IllegalStateException("RestApi must be configured before using requests");
        }

        httpClient = createHttpClient();
        executor = new RequestExecutor(1, RestApi.getConfiguration().getTimeout());
        userService = RestApi.getConfiguration().getRestAuthorizationService();
    }

    @NonNull
    private OkHttpClient createHttpClient() {
        int timeout = RestApi.getConfiguration().getTimeout();
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(1, timeout, TimeUnit.MILLISECONDS))
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS);

        final BasicAuthorization basicAuthorization = RestApi.getConfiguration().getBasicAuthorization();
        if (basicAuthorization != null) {
            clientBuilder.authenticator(new Authenticator() {
                @Override
                public okhttp3.Request authenticate(Route route, Response response) throws IOException {
                    okhttp3.Request.Builder requestBuilder = response.request()
                            .newBuilder()
                            .header("Authorization", basicAuthorization.getBasicAuthorization());
                    return requestBuilder.build();
                }
            });
        }
        return clientBuilder.build();
    }

    /**
     * {@inheritDoc}
     */
    RequestFuture<T> execute() {
        return execute(null);
    }

    /**
     * {@inheritDoc}
     */
    RequestFuture<T> execute(RequestListener<T> listener) {
        return execute(createRequestTask(), listener);
    }

    @NonNull
    private RequestFuture<T> execute(Callable<T> task, RequestListener<T> listener) {
        RequestFuture<T> future = executor.submit(task, ignoreErrorCallback ? null : RestApi.getConfiguration().getErrorCallback(), listener);
        executor.shutdown();
        return future;
    }

    /**
     * {@inheritDoc}
     */
    public void cancel() {
        executor.shutdownNow();
    }

    /**
     * Creates new instance of the task that have to be executed
     *
     * @return callable instance of the request task
     */
    @NonNull
    protected Callable<T> createRequestTask() {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                timer.start();
                T result = executeRequest();
                double time = timer.getElapsedTime();
                if (time < minExecutionTime) {
                    try {
                        Thread.sleep((long) (minExecutionTime - time));
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!disableLogging) {
                    RestApi.getLogger().vF("%s execution took: %.3fms", getClassCodeAnchor(), timer.getElapsedTime());
                }
                return result;
            }
        };
    }

    /**
     * This method invokes task to do by this request and return specified.
     *
     * @return requested object
     */
    protected T executeRequest() throws RequestException, IOException {
        checkInitialRequirements(Request.this);
        T mock = mock();
        if (mock != null) {
            if (!disableLogging) {
                RestApi.getLogger().i("Mock response:", getClassCodeAnchor());
            }
            return mock;
        }
        checkAccessToken();
        prepareRequest();
        HttpUrl url = getUrl();
        if (!disableLogging) {
            RestApi.getLogger().v("Start execution:", getClassCodeAnchor(), url);
        }
        Response response = request(url);
        T result = readResponse(response);
        response.close();
        return result;
    }

    @NonNull
    protected String getClassCodeAnchor() {
        Class<?> enclosingClass = getClass().getEnclosingClass();
        String name;
        if (enclosingClass != null) {
            name = enclosingClass.getSimpleName();
        } else {
            name = getClass().getSimpleName();
        }
        return "(" + name + ".java:1)";
    }

    /**
     * Mock of Request if this method returns non null object then http request will not be executed
     */
    protected T mock() {
        return null;
    }

    /**
     * Checks initial requirements before request execution e.g checks network connection. If any of requirement fails then {@link RequestException} will be thrown
     *
     * @throws RequestException exception with cause of fail
     */
    protected void checkInitialRequirements(Request<T> request) throws RequestException {
        try {
            InitialRequirements initialRequirements = RestApi.getConfiguration().getInitialRequirements();
            if (initialRequirements != null) {
                initialRequirements.onCheckRequirements(request);
            }
        } catch (InitialRequirementsException e) {
            throw new RequestException(e);
        }
    }

    /**
     * This method will be executed only if {@link #needAuthorization()} method returns true.
     * Method checks if user is logged in and access token is valid (if not then try to refresh it).
     * If Refresh token failed then user will be automatically logged out.
     */
    @WorkerThread
    protected void checkAccessToken() {
        if (!needAuthorization()) {
            return;
        }
        if (userService == null) {
            throw new UserServiceNotInitialized("Request is not properly configured to use UserService. Use method RestApi.Config.setRestAuthorizationService(RestAuthorizationService) to provide user service implementation");
        }

        if (!userService.isLogged()) {
            if (!userService.onNotLogged(this)) {
                return;
            }
        }

        LOCK.waitIfLocked();
        if (userService.isTokenValid()) {
            return;
        }
        LOCK.lock();
        try {
            userService.refreshToken();
        } catch (Exception e) {
            userService.logout();
            throw new RefreshTokenException("Problem during obtaining refresh token", e);
        } finally {
            LOCK.unlock();
        }
    }

    protected void ignoreErrorCallback() {
        ignoreErrorCallback = true;
    }

    protected boolean isErrorCallbackIgnored() {
        return ignoreErrorCallback;
    }

    protected void disableLogging() {
        disableLogging = true;
    }

    protected boolean isLoggingDisabled() {
        return ignoreErrorCallback;
    }

    /**
     * Mark that this request need authorization via {@link #ACCESS_TOKEN}
     */
    protected void setIsAuthorizedRequest() {
        setIsAuthorizedRequest(true);

    }

    /**
     * Mark that this request need (or not) authorization via {@link #ACCESS_TOKEN}
     */
    public void setIsAuthorizedRequest(boolean authorizedRequest) {
        this.authorizedRequest = authorizedRequest;
    }

    /**
     * Checks if in this request need to add {@link #ACCESS_TOKEN} to the url
     *
     * @return true if {@link #setIsAuthorizedRequest()} was called or {@link Request} implements {@link Authorizable} interface
     */
    protected boolean needAuthorization() {
        return authorizedRequest || this instanceof Authorizable;
    }

    /**
     * Prepare all request properties
     *
     * @see #setUrl(String)
     * @see #setUrlSegments(String...)
     * @see #putParameter(String, Object)
     * @see #putUrlParameter(String, Object)
     */
    protected abstract void prepareRequest();

    /**
     * Method that execute this request and returns response from the server. Response must be closed
     *
     * @see Response
     */
    @WorkerThread
    protected abstract Response request(HttpUrl url) throws IOException;

    /**
     * Reads response and parse to object or throws exception if execution failed
     */
    protected T readResponse(Response response) throws IOException, RequestException {
        if (response == null) {
            return null;
        }
        int status = response.code();
        String content = response.body().string();
        if (!disableLogging) {
            RestApi.getLogger().v("Response from:", getClassCodeAnchor() + "\n" + content);
        }
        if (isSuccess(response)) {
            Class<? extends Request> aClass = getClass();
            //noinspection unchecked
            return (T) getDeserializer().read(aClass, content);
        } else {
            throw getErrorDeserializer().read(status, content);
        }
    }

    /**
     * Checks if response is success. By default checks if response code is 200
     */
    protected boolean isSuccess(Response response) {
        return RestApi.getConfiguration().getSuccessStatusCodes().contains(response.code());
    }

    /**
     * Returns complete api url. If additional parameters should be added than it must be done before this call
     *
     * @see #putUrlParameter(String, Object)
     */
    protected HttpUrl getUrl() {
        if (urlSegments == null) {
            throw new IllegalStateException("Set url in " + this.getClass().getSimpleName() + " prepareRequest method");
        }
        HttpUrl url;
        if (!TextUtils.isEmpty(this.url)) {
            if (needAuthorization() && userService != null && userService.getAuthorization() != null) {
                String accessToken = ACCESS_TOKEN + "=" + userService.getAuthorization().getAccessToken();
                if (!this.url.contains(accessToken)) {
                    int questionMarkPosition = this.url.indexOf('?');
                    char paramSeparator = questionMarkPosition >= 0 ? '&' : '?';
                    this.url += paramSeparator + accessToken;
                }
            }
            url = HttpUrl.parse(this.url);

        } else {
            HttpUrl.Builder builder = new HttpUrl.Builder();
            builder.scheme(RestApi.getConfiguration().getScheme());
            if (RestApi.getConfiguration().getPort() >= 0) {
                builder.port(RestApi.getConfiguration().getPort());
            }
            builder.host(RestApi.getConfiguration().getHost());

            BasicAuthorization basicAuthorization = RestApi.getConfiguration().getBasicAuthorization();
            if (basicAuthorization != null) {
                builder.username(basicAuthorization.getUser())
                        .password(basicAuthorization.getPassword());
            }
            for (String segment : urlSegments) {
                builder.addPathSegment(segment);
            }
            for (Parameter param : urlParameters) {
                builder.addQueryParameter(param.getName(), String.valueOf(param.getValue()));
            }

            if (needAuthorization() && userService != null && userService.getAuthorization() != null) {
                builder.addQueryParameter(ACCESS_TOKEN, userService.getAuthorization().getAccessToken());
            }
            url = builder.build();
        }
        return url;
    }

    /**
     * Set api method url. If url will be sets by this method then all calls {@link #setUrlSegments(String...) setUrlSegments}, {@link #putParameter(String, Object) putParameter} or {@link #putUrlParameter(String, Object) putUrlParameter} will be ignored
     */
    protected void setUrl(String url) {
        this.url = url;
    }

    /**
     * Set minimal execution time of this request. Execution of this request will take at least <code>millis</code> milliseconds
     */
    protected void setMinExecutionTime(long millis) {
        this.minExecutionTime = millis;
    }

    /**
     * Set api method url based on {@link RestApi.Config}
     *
     * @param urlSegments path segments e.g: <p>
     *                    for address http://example.com/get/user  this method should be invoked: {@code setUrlSegments("get", "user");}
     */
    protected void setUrlSegments(String... urlSegments) {
        this.urlSegments = urlSegments;
    }

    /**
     * Adds parameter to URL's query string.
     */
    protected void putUrlParameter(@NonNull String name, @Nullable Object value) {
        getSerializer().serialize(urlParameters, name, value);
    }

    /**
     * Adds parameter to URL's query string.
     */
    protected void putUrlParameter(@Nullable Object value) {
        getSerializer().serialize(urlParameters, value);
    }

    /**
     * Adds parameter to request body
     */
    protected void putParameter(@NonNull String name, @Nullable Object value) {
        getSerializer().serialize(bodyParameters, name, value);
    }

    /**
     * Adds parameter to request body
     */
    protected void putParameter(@Nullable Object value) {
        getSerializer().serialize(bodyParameters, value);
    }

    protected void addHeader(@NonNull String name, @NonNull String value) {
        headerMap.put(name, value);
    }

    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>(headerMap);
        headers.putAll(RestApi.getConfiguration().getHeaders());
        return headers;
    }

    /**
     * Removes parameter from request body
     */
    protected void removeParameter(@NonNull String key) {
        Iterator<Parameter> iterator = bodyParameters.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = iterator.next();
            if (key.equals(parameter.getName())) {
                iterator.remove();
                return;
            }
        }
    }

    /**
     * Removes parameter from URL's query string.
     */
    protected void removeUrlParameter(@NonNull String key) {
        Iterator<Parameter> iterator = urlParameters.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = iterator.next();
            if (key.equals(parameter.getName())) {
                iterator.remove();
                return;
            }
        }
    }

    /**
     * Returns {@link RequestBody} with
     *
     * @return body or null if none of parameters was added
     */
    @Nullable
    protected RequestBody getRequestBody() {
        MediaType mediaType = getMediaType();
        if (MULTIPART_FORM_DATA.equals(mediaType)) {
            return getMultipartBody();
        } else if (APPLICATION_URLENCODED.equals(mediaType)) {
            return getFormBody();
        } else if (APPLICATION_JSON.equals(mediaType)) {
            return getJsonBody();
        } else {
            throw new IllegalStateException("Unsupported media type: " + mediaType.toString());
        }
    }

    /**
     * Checks if at least one of the parameters is a file
     */
    protected boolean isMultipartRequest() {
        for (Parameter parameter : bodyParameters) {
            if (parameter.isFile()) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private RequestBody getMultipartBody() {
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
        bodyBuilder.setType(MULTIPART_FORM_DATA);

        Iterator<Parameter> iterator = bodyParameters.iterator();
        while (iterator.hasNext()) {
            Parameter parameter = iterator.next();
            if (parameter.isFile()) {
                String name = parameter.getName();
                String path = parameter.getFilePath();
                if (!disableLogging) {
                    RestApi.getLogger().d(name + ":", path);
                }
                File file = new File(path);
                if (file.exists()) {
                    bodyBuilder.addFormDataPart(name, file.getName(), RequestBody.create(MediaType.parse("image/" + getFileExtension(file)), file));
                }
                iterator.remove();
            }
        }
        for (Parameter parameter : bodyParameters) {
            String name = parameter.getName();
            String value = String.valueOf(parameter.getValue());
            if (!disableLogging) {
                RestApi.getLogger().d(name + ":", value);
            }
            if (!TextUtils.isEmpty(value)) {
                bodyBuilder.addFormDataPart(name, value);
            }
        }
        return bodyBuilder.build();
    }

    private String getFileExtension(@NonNull File file) {
        String fileName = file.getName();
        int beginIndex = fileName.lastIndexOf(".") + 1;
        if (beginIndex == 0) {
            return "*";
        }
        return fileName.substring(beginIndex);
    }

    @NonNull
    private RequestBody getFormBody() {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Parameter parameter : bodyParameters) {
            String name = parameter.getName();
            String value = String.valueOf(parameter.getValue());
            if (!disableLogging) {
                RestApi.getLogger().d(name + ":", value);
            }
            if (!TextUtils.isEmpty(value)) {
                bodyBuilder.add(name, value);
            }
        }
        return bodyBuilder.build();
    }

    private RequestBody getJsonBody() {
        if (getSerializer() instanceof JsonSerializer) {
            return RequestBody.create(APPLICATION_JSON, ((JsonSerializer) getSerializer()).toJsonString(bodyParameters));
        } else {
            throw new IllegalStateException("Json media type requires JsonSerializer. Set JsonSerializer via RestApi.Config().setSerializer()");
        }
    }

    protected MediaType getMediaType() {
        return isMultipartRequest() ? MULTIPART_FORM_DATA : mediaType;
    }

    protected Serializer getSerializer() {
        return RestApi.getConfiguration().getSerializer();
    }

    protected Deserializer getDeserializer() {
        return RestApi.getConfiguration().getDeserializer();
    }

    protected ErrorDeserializer getErrorDeserializer() {
        return RestApi.getConfiguration().getErrorDeserializer();
    }


}
