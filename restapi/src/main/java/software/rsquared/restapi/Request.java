package software.rsquared.restapi;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import software.rsquared.restapi.exceptions.InitialRequirementsException;
import software.rsquared.restapi.exceptions.RefreshTokenException;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.exceptions.UserServiceNotInitialized;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonSerializer;
import software.rsquared.restapi.serialization.Serializer;

import static software.rsquared.restapi.RestApiUtils.enableTls12OnPreLollipop;

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

	protected static final String CONTENT_TYPE = "Content-Type";
	protected static final String KEY_BASIC_AUTHORIZATION = "BasicAuthorization";

	private static final Executor DEFAULT_UI_EXECUTOR = new MainThreadExecutor();

	@Nullable
	private OkHttpClient httpClient;

	@Nullable
	private Executor ioExecutor;

	@NonNull
	private Executor uiExecutor = DEFAULT_UI_EXECUTOR;

	@NonNull
	private final HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

	@NonNull
	private final Map<String, Object> bodyParameters = new LinkedHashMap<>();

	@NonNull
	private final Map<String, Object> queryParameters = new LinkedHashMap<>();

	@NonNull
	private final Map<String, String> headerMap = new HashMap<>();

	@NonNull
	private final Timer timer = new Timer();

	private long minExecutionTime = 0;

	@Nullable
	private String logParameters;

	@Nullable
	private final String logCreateCodeLine;

	private RequestTask task;

	private AtomicBoolean executed = new AtomicBoolean(false);

	private T result;

	/**
	 * Initial constructor for request.
	 */
	protected Request() {
		logCreateCodeLine = RestApiUtils.getClassCodeLine(getClass().getName());
	}

	protected void execute(RestApi api, RequestListener<T> listener) {
		if (executed.compareAndSet(false, true)) {
			task = createTask(api, listener);
			executeTask(api, listener);
		} else {
			throw new IllegalStateException("Request currently executed!");
		}
	}

	private void executeTask(RestApi api, RequestListener<T> listener) {
		Executor uiExecutor = api.getUiExecutor();
		if (uiExecutor != null) {
			this.uiExecutor = uiExecutor;
		}

		ioExecutor = api.getIOExecutor();
		if (ioExecutor != null) {
			ioExecutor.execute(task);
		} else {
			throw new IllegalStateException("RestApi required to specify IOExecutor!");
		}
	}

	public void cancel() {
		if (task != null) {
			task.cancel();
		}
	}

	/**
	 * Creates new instance of the task that have to be executed
	 *
	 * @param api
	 * @param listener
	 * @return callable instance of the request task
	 */
	@NonNull
	protected RequestTask createTask(RestApi api, RequestListener<T> listener) {
		return new RequestTask() {
			@Override
			protected void execute() throws RequestException {
				timer.start();
				result = executeRequest(api);
				waitForMinExecutionTime();
				getLogger().debug(logCreateCodeLine, String.format(Locale.getDefault(), "Execution of " + getClassName() + " took: %.3fms", timer.getElapsedTime()));
			}

			@Override
			protected void onFailed(RequestException e) {
			}
		};
	}

	/**
	 * This method invokes task to do by this request and return specified.
	 *
	 * @param api
	 * @return requested object
	 */
	protected T executeRequest(RestApi api) throws RequestException {
		computeCheckers(api);
		T mock = getMockResponse(api.getMockFactory());
		if (mock != null) {
			getLogger().debug(logCreateCodeLine, "Mocked response");
			return mock;
		}
		initUrl(api);
		prepareRequest();
		Call request = createRequest(urlBuilder.build());
		getLogger().debug(logCreateCodeLine, "Start execution " + getClassName() + ":\n" + logParameters);
		Response response = request.execute();
		T result = readResponse(response);
		response.close();
		return result;
	}

	/**
	 * Checks initial requirements before request execution e.g checks network connection. If any of requirement fails then {@link RequestException} will be thrown
	 *
	 * @throws RequestException exception with cause of fail
	 */
	protected void computeCheckers(RestApi api) throws RequestException {
		List<Checker> checkers = api.getCheckers();
		if (checkers != null) {
			for (Checker checker : checkers) {
				checker.check(this);
			}
		}
	}

	/**
	 * Mock of Request if this method returns non null object then http request will not be executed
	 */
	protected T getMockResponse(MockFactory mockFactory) {
		return mockFactory == null ? null : mockFactory.getMockResponse(this);
	}

	private void initUrl(RestApi api) {
		String baseUrl = api.getBaseUrl();
		Uri uri = Uri.parse(baseUrl);
		urlBuilder.scheme(uri.getScheme());
		urlBuilder.host(uri.getHost());
		urlBuilder.port(uri.getPort());

		BasicAuthorization basicAuthorization = api.getBasicAuthorization();
		if (basicAuthorization != null) {
			urlBuilder.username(basicAuthorization.getUser());
			urlBuilder.password(basicAuthorization.getPassword());
		} else {
			String userInfo = uri.getUserInfo();
			if (!TextUtils.isEmpty(userInfo)) {
				String[] parts = userInfo.split(":", 2);
				urlBuilder.username(parts[0]);
				if (parts.length > 1) {
					urlBuilder.password(parts[1]);
				}
			}
		}


		List<String> segments = uri.getPathSegments();
		if (segments != null) {
			for (String segment : segments) {
				urlBuilder.addPathSegment(segment);
			}
		}

		Set<String> parameterNames = uri.getQueryParameterNames();
		if (parameterNames != null) {
			for (String name : parameterNames) {
				urlBuilder.setQueryParameter(name, uri.getQueryParameter(name));
			}
		}

		String fragment = uri.getFragment();
		if (!TextUtils.isEmpty(fragment)) {
			urlBuilder.fragment(fragment);
		}
	}

	/**
	 * Prepare all request properties
	 *
	 * @see #setUrl(String)
	 * @see #setUrlSegments(String...)
	 * @see #putParameter(String, Object)
	 * @see #putQueryParameter(String, Object)
	 */
	protected abstract void prepareRequest();

	/**
	 * Method that execute this request and returns response from the server. Response must be closed
	 *
	 * @see Response
	 */
	@WorkerThread
	protected abstract Call createRequest(HttpUrl url) throws RequestException;

	/**
	 * Returns complete api url. If additional parameters should be added than it must be done before this call
	 *
	 * @see #putUrlParameter(String, Object)
	 */
	protected HttpUrl getUrl() {
		if (urlSegments == null && TextUtils.isEmpty(this.url)) {
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


			RestApiConfiguration configuration = getConfiguration();
			HttpUrl.Builder builder = new HttpUrl.Builder();
			builder.scheme(configuration.getScheme());
			if (configuration.getPort() >= 0) {
				builder.port(configuration.getPort());
			}
			builder.host(configuration.getHost());

			BasicAuthorization basicAuthorization = configuration.getBasicAuthorization();
			if (basicAuthorization != null) {
				builder.username(basicAuthorization.getUser())
						.password(basicAuthorization.getPassword());
			}
			for (String segment : urlSegments) {
				builder.addPathSegment(segment);
			}
			for (Parameter param : queryParameters) {
				builder.addQueryParameter(param.getName(), String.valueOf(param.getValue()));
			}

			if (needAuthorization() && userService != null && userService.getAuthorization() != null) {
				builder.addQueryParameter(ACCESS_TOKEN, userService.getAuthorization().getAccessToken());
			}
			url = builder.build();
		}
		return url;
	}

	private void waitForMinExecutionTime() {
		double time = timer.getElapsedTime();
		if (time < minExecutionTime) {
			try {
				Thread.sleep((long) (minExecutionTime - time));
			} catch (InterruptedException ignored) {
			}
		}
	}

	@NonNull
	protected OkHttpClient.Builder createHttpClient(RestApiConfiguration configuration) {
		int timeout = configuration.getTimeout();
		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
				.connectionPool(new ConnectionPool(1, timeout, TimeUnit.MILLISECONDS))
				.followSslRedirects(true)
				.connectTimeout(timeout, TimeUnit.MILLISECONDS)
				.readTimeout(timeout, TimeUnit.MILLISECONDS);
		ConnectionSpec connectionSpec = configuration.getConnectionSpec();
		if (connectionSpec != null) {
			clientBuilder.connectionSpecs(Collections.singletonList(connectionSpec));
		}

		CertificatePinner certificatePinner = configuration.getCertificatePinner();
		if (certificatePinner != null) {
			clientBuilder.certificatePinner(certificatePinner);
		}

		final BasicAuthorization basicAuthorization = configuration.getBasicAuthorization();
		if (basicAuthorization != null) {
			clientBuilder.authenticator((route, response) -> {
				okhttp3.Request.Builder requestBuilder = response.request()
						.newBuilder()
						.header("Authorization", basicAuthorization.getBasicAuthorization());
				return requestBuilder.build();
			});
		}
		if (configuration.isEnableTls12OnPreLollipop()) {
			return enableTls12OnPreLollipop(clientBuilder);
		}
		return clientBuilder;
	}


	@NonNull
	protected String getClassName() {
		String name = this.getClass().getName();
		String[] parts = name.split("\\.");
		if (parts.length > 0 && !TextUtils.isEmpty(parts[parts.length - 1])) {
			String classPart = parts[parts.length - 1];
			parts = classPart.split("\\$");
			return parts.length > 0 && !TextUtils.isEmpty(parts[parts.length - 1]) ? parts[parts.length - 1] : classPart;
		} else {
			return "Request";
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
				cancel();
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
			if (!userService.onRefreshTokenFailed(new RefreshTokenException("Problem during obtaining refresh token", e))) {
				cancel();
				//noinspection UnnecessaryReturnStatement
				return;
			}
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
		return disableLogging;
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
	 * Reads response and parse to object or throws exception if execution failed
	 */
	protected T readResponse(Response response) throws IOException, RequestException {
		if (response == null) {
			return null;
		}
		int status = response.code();
		ResponseBody body = response.body();
		String content = null;
		if (body != null) {
			content = body.string();
		}
		if (!disableLogging) {
			getLogger().verbose(requestCodeLine.toString(), "Response from " + getClassName() + " (" + requestUrl + "):\n" + content);
		}
		if (isSuccess(response)) {
			T result = readResult(content);
			readHeaders(response.headers(), result);
			return result;
		} else {
			throw getErrorDeserializer().read(status, content);
		}
	}

	protected T readResult(String content) throws IOException {
		Class<? extends Request> aClass = getClass();
		return getDeserializer().read(aClass, content);
	}

	protected void readHeaders(Headers headers, T result) {
	}

	/**
	 * Checks if response is success. By default checks if response code is 200
	 */
	protected boolean isSuccess(Response response) {
		return getConfiguration().getSuccessStatusCodes().contains(response.code());
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
	 * Set api method url based on {@link RestApiConfiguration}
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
		getSerializer().serialize(queryParameters, name, value);
	}

	/**
	 * Adds parameter to URL's query string.
	 */
	protected void putUrlParameter(@Nullable Object value) {
		getSerializer().serialize(queryParameters, value);
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
		headers.putAll(getConfiguration().getHeaders());
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
		Iterator<Parameter> iterator = queryParameters.iterator();
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
					requestLog.append("\n").append(name).append(": ").append(path);
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
				requestLog.append("\n").append(name).append(": ").append(value);
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
				requestLog.append("\n").append(name).append(": ").append(value);
			}
			if (!TextUtils.isEmpty(value)) {
				bodyBuilder.add(name, value);
			}
		}
		return bodyBuilder.build();
	}

	private RequestBody getJsonBody() {
		if (getSerializer() instanceof JsonSerializer) {
			String body = ((JsonSerializer) getSerializer()).toJsonString(bodyParameters);
			if (!disableLogging) {
				requestLog.append("\n").append(body);
			}
			return RequestBody.create(APPLICATION_JSON, body);
		} else {
			throw new IllegalStateException("Json media type requires JsonSerializer. Set JsonSerializer via RestApi.Config().setSerializer()");
		}
	}

	protected MediaType getMediaType() {
		return isMultipartRequest() ? MULTIPART_FORM_DATA : mediaType;
	}

	protected Serializer getSerializer() {
		return getConfiguration().getSerializer();
	}

	protected Deserializer getDeserializer() {
		return getConfiguration().getDeserializer();
	}

	protected ErrorDeserializer getErrorDeserializer() {
		return getConfiguration().getErrorDeserializer();
	}

	protected RestApiLogger getLogger() {
		return getConfiguration().getLogger();
	}

	protected RestApiConfiguration getConfiguration() {
		return RestApi.getConfiguration();
	}

}
