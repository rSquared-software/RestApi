package software.rsquared.restapi;

import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Pair;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import software.rsquared.restapi.exceptions.RequestException;
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

	protected static final String CONTENT_TYPE = "Content-Type";
	protected static final String KEY_BASIC_AUTHORIZATION = "BasicAuth";

	@NonNull
	private final HttpUrl.Builder urlBuilder = new HttpUrl.Builder();

	@Nullable
	private HttpUrl url;

	@NonNull
	private final List<Parameter> bodyParameters = new ArrayList<>();
	@NonNull
	private final List<Parameter> queryParameters = new ArrayList<>();

	@NonNull
	private final Map<String, String> headerMap = new HashMap<>();

	@NonNull
	private final Timer timer = new Timer();

	private long minExecutionTime = 0;

	@Nullable
	private String logParameters;

	@Nullable
	private final String logCreateCodeLine;

	private AtomicBoolean executed = new AtomicBoolean(false);

	private T result;

	private RestApi api;

	@Nullable
	private RequestTask<T> task;

	@Nullable
	private Call requestCall;


	private AtomicBoolean cancelled = new AtomicBoolean(false);
	private OkHttpClient httpClient;

	/**
	 * Initial constructor for request.
	 */
	protected Request() {
		logCreateCodeLine = RestApiUtils.getClassCodeLine(getClass().getName());
	}

	@AnyThread
	protected void execute(RestApi api, RequestListener<T> listener) {
		RequestTask<T> task = beforeExecution(api, listener);
		executeTask(api, task);
	}

	@WorkerThread
	protected T executeSync(RestApi api) throws RequestException {
		RequestTask<T> task = beforeExecution(api, null);
		executeTaskSync(task);
		return result;
	}

	protected RequestTask<T> beforeExecution(RestApi api, RequestListener<T> listener) {
		if (executed.compareAndSet(false, true)) {
			this.api = api;
			return createTask(listener);
		} else {
			throw new IllegalStateException("Request currently executed!");
		}
	}

	@WorkerThread
	private void executeTaskSync(RequestTask<T> task) throws RequestException {
		task.onPreExecute();
		try {
			task.onSuccess(task.execute());
		} catch (RequestException e) {
			task.onFailed(e);
			throw e;
		}
		task.onPostExecute();
	}

	@AnyThread
	private void executeTask(RestApi api, RequestTask<T> task) {
		if (api.getNetworkExecutor() != null) {
			api.getNetworkExecutor().execute(task);
		} else {
			throw new IllegalStateException("NetworkExecutor is required!");
		}
	}

	public void cancel() {
		if (cancelled.compareAndSet(false, true)) {
			if (task != null) {
				task.onCancelled();
			}
			if (requestCall != null) {
				requestCall.cancel();
			}
		} else {
			getLogger().debug(logCreateCodeLine, "Already cancelled");
		}
	}

	/**
	 * Creates new instance of the task that have to be executed
	 *
	 * @param listener
	 * @return callable instance of the request task
	 */
	@NonNull
	protected RequestTask<T> createTask(@Nullable RequestListener<T> listener) {
		return new RequestTask<T>(listener) {
			@Override
			protected void onPreExecute() {
				Executor uiExecutor = api.getUiExecutor();
				if (listener != null) {
					uiExecutor.execute(listener::onPreExecute);
				}
			}

			@Override
			protected T execute() throws RequestException {
				timer.start();
				result = executeRequest();
				waitForMinExecutionTime();
				getLogger().debug(logCreateCodeLine, String.format(Locale.getDefault(), "Execution of " + getClassName() + " took: %.3fms", timer.getElapsedTime()));
				return result;
			}

			@Override
			protected void onSuccess(T result) {
				Executor uiExecutor = api.getUiExecutor();
				if (listener != null) {
					uiExecutor.execute(() -> listener.onSuccess(result));
				}
			}

			@Override
			protected void onFailed(RequestException e) {
				Executor uiExecutor = api.getUiExecutor();
				if (listener != null) {
					uiExecutor.execute(() -> listener.onFailed(e));
				}
			}

			@Override
			protected void onPostExecute() {
				Executor uiExecutor = api.getUiExecutor();
				if (listener != null) {
					uiExecutor.execute(listener::onPostExecute);
				}
			}


			@Override
			protected void onCancelled() {
				Executor uiExecutor = api.getUiExecutor();
				if (listener != null) {
					uiExecutor.execute(listener::onCanceled);
				}
			}
		};
	}

	/**
	 * This method invokes task to do by this request and return specified.
	 *
	 * @return requested object
	 */
	protected T executeRequest() throws RequestException {
		computeCheckers();
		result = getMockResponse(api.getMockFactory());
		if (result != null) {
			getLogger().debug(logCreateCodeLine, "Mocked response");
		} else {
			setUrl();
			HeaderFactory headerFactory = api.getHeaderFactory();
			if (headerFactory != null) {
				headerMap.putAll(headerFactory.getHeaders(this));
			}
			prepareRequest();
			computeAuthenticator();
			buildUrl();
			if (!cancelled.get()) {
				httpClient = createHttpClient();
				requestCall = createRequest(httpClient, url, headerMap, getRequestBody());

				getLogger().debug(logCreateCodeLine, "Start execution " + getClassName() + ":\n" + url + "\n" + logParameters);
				if (!cancelled.get()) {
					try (Response response = requestCall.execute()) {
						result = readResponse(response);
					} catch (IOException e) {
						throw new RequestException(e);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Checks initial requirements before request execution e.g checks network connection. If any of requirement fails then {@link RequestException} will be thrown
	 *
	 * @throws RequestException exception with cause of fail
	 */
	protected void computeCheckers() throws RequestException {
		List<Checker> checkers = api.getCheckerList();
		for (Checker checker : checkers) {
			checker.check(this);
		}
	}

	protected void computeAuthenticator() {
		final RequestAuthenticator authenticator = api.getRequestAuthenticator();
		if (authenticator == null || !authenticator.isAuthorizable(this)) {
			return;
		}

		authenticator.checkAndAdd(this);

		for (Pair<String, Object> pair : authenticator.getQueryParameters()) {
			if (pair.first == null) {
				putQueryParameter(pair.second);
			} else {
				putQueryParameter(pair.first, pair.second);
			}
		}
		for (Pair<String, Object> pair : authenticator.getParameters()) {
			if (pair.first == null) {
				putParameter(pair.second);
			} else {
				putParameter(pair.first, pair.second);
			}
		}
		for (Pair<String, String> pair : authenticator.getHeaders()) {
			addHeader(pair.first, pair.second);
		}
	}

	protected boolean isAuthorizable() {
		return this instanceof Authorizable;
	}

	/**
	 * Mock of Request if this method returns non null object then http request will not be executed
	 */
	protected T getMockResponse(MockFactory mockFactory) {
		return mockFactory == null ? null : mockFactory.getMockResponse(this);
	}

	private void setUrl() {
		setUrl(api.getUrl());
		BasicAuth basicAuth = api.getBasicAuth();
		if (basicAuth != null) {
			setBasicAuthorization(basicAuth.getUser(), basicAuth.getPassword());
		}
	}

	protected void buildUrl() {
		for (Parameter param : queryParameters) {
			urlBuilder.addQueryParameter(param.getName(), String.valueOf(param.getValue()));
		}
		this.url = urlBuilder.build();
	}

	/**
	 * Set api method url.
	 */
	protected void setUrl(@NonNull String url) {
		Uri uri = Uri.parse(url);
		setScheme(uri.getScheme());
		setHost(uri.getHost());
		setPort(uri.getPort());

		String userInfo = uri.getUserInfo();
		if (!TextUtils.isEmpty(userInfo)) {
			String[] parts = userInfo.split(":", 2);
			if (parts.length > 1) {
				setBasicAuthorization(parts[0], parts[1]);
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
			setFragment(fragment);
		}
	}

	protected void setScheme(String scheme) {
		urlBuilder.scheme(scheme);
	}

	protected void setBasicAuthorization(String username, String password) {
		urlBuilder.username(username);
		urlBuilder.password(password);
	}

	protected void setHost(String host) {
		urlBuilder.host(host);
	}

	protected void setPort(int port) {
		if (port > 0) {
			urlBuilder.port(port);
		}
	}

	protected void setFragment(String fragment) {
		urlBuilder.fragment(fragment);
	}

	/**
	 * Set api method url
	 *
	 * @param segments path segments e.g: <p>
	 *                 for address http://example.com/get/user  this method should be invoked: {@code addPathSegments("get", "user");}
	 */
	protected void addPathSegments(String... segments) {
		if (segments != null) {
			for (String segment : segments) {
				this.urlBuilder.addPathSegment(segment);
			}
		}
	}

	/**
	 * Adds parameter to URL's query string.
	 */
	protected void putQueryParameter(@NonNull String name, @Nullable Object value) {
		getSerializer().serialize(queryParameters, name, value);
	}

	/**
	 * Adds parameter to URL's query string.
	 */
	protected void putQueryParameter(@Nullable Object value) {
		String name = getNameFor(value);
		if (TextUtils.isEmpty(name)) {
			throw new RuntimeException("Unknown name!");
		}
		getSerializer().serialize(queryParameters, name, value);
	}

	/**
	 * Adds parameter to request body
	 */
	void putParameter(@NonNull String name, @Nullable Object value) {
		getSerializer().serialize(bodyParameters, name, value);
	}

	/**
	 * Adds parameter to request body
	 */
	void putParameter(@Nullable Object value) {
		String name = getNameFor(value);
		if (TextUtils.isEmpty(name)) {
			throw new RuntimeException("Unknown name!");
		}
		getSerializer().serialize(bodyParameters, name, value);
	}

	protected String getNameFor(Object object) {
		if (isRestObject(object)) {
			return getRestObjectName(object);
		}
		return null;
	}

	private boolean isRestObject(Object object) {
		return object != null && object.getClass().getAnnotation(RestObject.class) != null;
	}

	private String getRestObjectName(Object object) {
		RestObject restObject = object.getClass().getAnnotation(RestObject.class);
		if (!TextUtils.isEmpty(restObject.value())) {
			return restObject.value();
		} else {
			return object.getClass().getSimpleName();
		}
	}

	protected void addHeader(@NonNull String name, @NonNull String value) {
		headerMap.put(name, value);
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
	protected void removeQueryParameter(@NonNull String key) {
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

	protected MediaType getMediaType() {
		if (isMultipartRequest()) {
			return MULTIPART_FORM_DATA;
		} else if (getSerializer() instanceof JsonSerializer) {
			return APPLICATION_JSON;
		} else {
			return APPLICATION_URLENCODED;
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
	protected RequestBody getMultipartBody() {
		MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
		bodyBuilder.setType(MULTIPART_FORM_DATA);

		StringBuilder parametersStringBuilder = new StringBuilder();
		Iterator<Parameter> iterator = bodyParameters.iterator();
		while (iterator.hasNext()) {
			Parameter parameter = iterator.next();
			if (parameter.isFile()) {
				String name = parameter.getName();
				String path = parameter.getFilePath();
				appendParameterWithNewLine(parametersStringBuilder, name, path);
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
			appendParameterWithNewLine(parametersStringBuilder, name, value);
			if (!TextUtils.isEmpty(value)) {
				bodyBuilder.addFormDataPart(name, value);
			}
		}
		logParameters = parametersStringBuilder.toString();
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
	protected RequestBody getFormBody() {
		FormBody.Builder bodyBuilder = new FormBody.Builder();
		StringBuilder parametersStringBuilder = new StringBuilder();
		for (Parameter parameter : bodyParameters) {
			String name = parameter.getName();
			String value = String.valueOf(parameter.getValue());
			appendParameterWithNewLine(parametersStringBuilder, name, value);
			if (!TextUtils.isEmpty(value)) {
				bodyBuilder.add(name, value);
			}
		}
		logParameters = parametersStringBuilder.toString();
		return bodyBuilder.build();
	}

	protected RequestBody getJsonBody() {
		if (getSerializer() instanceof JsonSerializer) {
			String body = ((JsonSerializer) getSerializer()).toJsonString(bodyParameters);
			logParameters = body;
			return RequestBody.create(APPLICATION_JSON, body);
		} else {
			throw new IllegalStateException("Json media type requires JsonSerializer. Set JsonSerializer via RestApi.Config().setSerializer()");
		}
	}

	private void appendParameterWithNewLine(StringBuilder parametersStringBuilder, String name, String value) {
		if (parametersStringBuilder.length() > 0) {
			parametersStringBuilder.append("\n");
		}
		parametersStringBuilder.append(name).append(": ").append(value);
	}

	/**
	 * Prepare all request properties
	 *
	 * @see #setUrl(String)
	 * @see #addPathSegments(String...)
	 * @see #putParameter(String, Object)
	 * @see #putQueryParameter(String, Object)
	 */
	protected abstract void prepareRequest();

	/**
	 * Method that execute this request and returns response from the server. Response must be closed
	 *
	 * @see Response
	 */
	@NonNull
	@WorkerThread
	protected abstract Call createRequest(OkHttpClient client, HttpUrl url, Map<String, String> headers, RequestBody requestBody) throws RequestException;

	protected void waitForMinExecutionTime() {
		double time = timer.getElapsedTime();
		if (time < minExecutionTime) {
			try {
				Thread.sleep((long) (minExecutionTime - time));
			} catch (InterruptedException ignored) {
			}
		}
	}

	@NonNull
	protected OkHttpClient createHttpClient() {
		long timeout = api.getTimeout();
		OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
				.connectionPool(new ConnectionPool(1, timeout, TimeUnit.MILLISECONDS))
				.followSslRedirects(true)
				.connectTimeout(timeout, TimeUnit.MILLISECONDS)
				.readTimeout(timeout, TimeUnit.MILLISECONDS);

		final BasicAuth basicAuth = api.getBasicAuth();
		if (basicAuth != null) {
			clientBuilder.authenticator((route, response) -> {
				okhttp3.Request.Builder requestBuilder = response.request()
						.newBuilder()
						.header("Authorization", basicAuth.getCredentials());
				return requestBuilder.build();
			});
		}
		if (api.isEnableTls12OnPreLollipop()) {
			return RestApiUtils.enableTls12OnPreLollipop(clientBuilder).build();
		} else {
			return clientBuilder.build();
		}
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
	 * Reads response and parse to object or throws exception if execution failed
	 */
	protected T readResponse(Response response) throws RequestException, IOException {
		if (response == null) {
			return null;
		}
		int status = response.code();
		ResponseBody body = response.body();
		String content = null;
		if (body != null) {
			content = body.string();
		}
		getLogger().verbose(logCreateCodeLine, "Response from " + getClassName() + " (" + url + "):\n" + content);
		if (isSuccess(response)) {
			T result = readResult(content);
			readHeaders(response.headers(), result);
			return result;
		} else {
			throw getErrorDeserializer().deserialize(status, content);
		}
	}

	protected T readResult(String content) throws IOException {
		return getDeserializer().deserialize(getClass(), getResultType(), content);
	}

	/**
	 * Returns result type
	 */
	protected TypeReference<T> getResultType() {
		return new RequestTypeReference<>(this);
	}

	protected void readHeaders(Headers headers, T result) {
	}

	/**
	 * Checks if response is success. By default checks if response code is 200
	 */
	protected boolean isSuccess(Response response) {
		return api.getSuccessStatusCodes().contains(response.code());
	}


	/**
	 * Set minimal execution time of this request. Execution of this request will take at least <code>millis</code> milliseconds
	 */
	protected void setMinExecutionTime(long millis) {
		this.minExecutionTime = millis;
	}

	protected Serializer getSerializer() {
		return api.getSerializer();
	}

	protected Deserializer getDeserializer() {
		return api.getDeserializer();
	}

	protected ErrorDeserializer getErrorDeserializer() {
		return api.getErrorDeserializer();
	}

	protected RestApiLogger getLogger() {
		return api.getLogger();
	}
}
