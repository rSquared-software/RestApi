package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.MediaType;
import software.rsquared.restapi.listeners.ErrorCallback;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonDeserializer;
import software.rsquared.restapi.serialization.JsonErrorDeserializer;
import software.rsquared.restapi.serialization.JsonSerializer;
import software.rsquared.restapi.serialization.ObjectToFormSerializer;
import software.rsquared.restapi.serialization.Serializer;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("ALL")
public class RestApiConfiguration {

	public static final String HTTP = "http";
	public static final String HTTPS = "https";

	private Set<Integer> successStatusCodes = new HashSet<>(Collections.singletonList(200));
	private int timeout = 60 * 1000;
	private String scheme = HTTP;
	private String host;
	private int port = -1;
	private MediaType mediaType = Request.APPLICATION_URLENCODED;
	@Nullable
	private BasicAuthorization basicAuthorization;
	@Nullable
	private InitialRequirements initialRequirements;

	private RestApiLogger logger = RestApiLoggerFactory.create();

	@NonNull
	private ErrorDeserializer errorDeserializer = new JsonErrorDeserializer();

	@NonNull
	private Deserializer deserializer = new JsonDeserializer();

	@NonNull
	private Serializer serializer = new ObjectToFormSerializer();

	private MockFactory mockFactory;

	private RestAuthorizationService restAuthorizationService;

	private Map<String, String> headers = new HashMap<>();

	private ErrorCallback errorCallback;

	private ConnectionSpec connectionSpec;

	private CertificatePinner certificatePinner;

	private boolean enableTls12OnPreLollipop = false;

	/**
	 * Timeout for the connections.
	 * A value of 0 means no timeout, otherwise values must be between 1 and Integer.MAX_VALUE milliseconds.<p>
	 * default: 1min
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Sets the default connect timeout for the connections.
	 * A value of 0 means no timeout, otherwise values must be between 1 and Integer.MAX_VALUE milliseconds.<p>
	 * default: 1min
	 */
	public RestApiConfiguration setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Url scheme - http or https
	 * <p>
	 * default: http
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * Sets the default url scheme - http or https
	 * <p>
	 * default: http
	 */
	public RestApiConfiguration setScheme(@NonNull String scheme) {
		this.scheme = scheme;
		return this;
	}

	/**
	 * Rest Api host - either a regular hostname, International Domain Name, IPv4 address, or IPv6
	 * address.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Rest Api host - either a regular hostname, International Domain Name, IPv4 address, or IPv6
	 * address.
	 */
	public RestApiConfiguration setHost(@NonNull String host) {
		this.host = host;
		return this;
	}

	/**
	 * Port for communication with rest api
	 * <p>
	 * default: 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
	 * otherwise.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Port for communication with rest api
	 * <p>
	 * default: 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
	 * otherwise.
	 */
	public RestApiConfiguration setPort(int port) {
		this.port = port;
		return this;
	}

	/**
	 * Basic authorization for all requests
	 */
	@Nullable
	public BasicAuthorization getBasicAuthorization() {
		return basicAuthorization;
	}

	/**
	 * Sets basic authorization for all requests
	 */
	public RestApiConfiguration setAuthorization(@NonNull String user, @NonNull String password) {
		if (TextUtils.isEmpty(user) || TextUtils.isEmpty(password)) {
			basicAuthorization = null;
			return this;
		}
		basicAuthorization = new BasicAuthorization(user, password);
		return this;
	}

	/**
	 * Initial restrictions for all requests
	 */
	@Nullable
	public InitialRequirements getInitialRequirements() {
		return initialRequirements;
	}

	/**
	 * Initial restrictions for all requests
	 */
	public RestApiConfiguration setInitialRequirements(@NonNull InitialRequirements initialRequirements) {
		this.initialRequirements = initialRequirements;
		return this;
	}

	/**
	 * Response deserializer
	 * <p>
	 * default: {@link JsonDeserializer JsonDeserializer}
	 */
	@NonNull
	public Deserializer getDeserializer() {
		return deserializer;
	}


	/**
	 * Response deserializer
	 * <p>
	 * default: {@link JsonDeserializer JsonDeserializer}
	 */
	public RestApiConfiguration setDeserializer(@NonNull Deserializer deserializer) {
		this.deserializer = deserializer;
		return this;
	}


	/**
	 * Request parameters serializer
	 * <p>
	 * default: {@link JsonSerializer JsonSerializer}
	 */
	@NonNull
	public Serializer getSerializer() {
		return serializer;
	}


	/**
	 * Request parameters serializer
	 * <p>
	 * default: {@link JsonSerializer JsonSerializer}
	 */
	public RestApiConfiguration setSerializer(@NonNull Serializer serializer) {
		this.serializer = serializer;
		return this;
	}

	/**
	 * Authorization service
	 * <p>
	 * default: null
	 */
	@Nullable
	public RestAuthorizationService getRestAuthorizationService() {
		return restAuthorizationService;
	}

	/**
	 * Authorization service
	 * <p>
	 * default: null
	 */
	public RestApiConfiguration setRestAuthorizationService(RestAuthorizationService authorizationService) {
		restAuthorizationService = authorizationService;
		return this;
	}

	/**
	 * Error response deserializer
	 * <p>
	 * default: {@link JsonErrorDeserializer JsonErrorDeserializer}
	 */
	@NonNull
	public ErrorDeserializer getErrorDeserializer() {
		return errorDeserializer;
	}

	/**
	 * Error response deserializer
	 * <p>
	 * default: {@link JsonErrorDeserializer JsonErrorDeserializer}
	 */
	public RestApiConfiguration setErrorDeserializer(@NonNull ErrorDeserializer errorDeserializer) {
		this.errorDeserializer = errorDeserializer;
		return this;
	}

	/**
	 * Add header to all requests
	 */
	public RestApiConfiguration addHeader(String name, String value) {
		headers.put(name, value);
		return this;
	}

	/**
	 * remove header added by {@link #addHeader(String, String)}
	 */
	public RestApiConfiguration removeHeader(String name) {
		headers.remove(name);
		return this;
	}

	/**
	 * Get headers map
	 */
	@NonNull
	public Map<String, String> getHeaders() {
		return headers;
	}

	/**
	 * Get callback that will be invoked if request failed
	 */
	@Nullable
	public ErrorCallback getErrorCallback() {
		return errorCallback;
	}


	public RestApiConfiguration setErrorCallback(@Nullable ErrorCallback errorCallback) {
		this.errorCallback = errorCallback;
		return this;
	}

	public Set<Integer> getSuccessStatusCodes() {
		return successStatusCodes;
	}

	public RestApiConfiguration setSuccessStatusCodes(Set<Integer> successStatusCodes) {
		this.successStatusCodes = successStatusCodes;
		return this;
	}

	public RestApiConfiguration setSuccessStatusCodes(int... successStatusCodes) {
		this.successStatusCodes = new HashSet<>();
		for (int code : successStatusCodes) {
			this.successStatusCodes.add(code);
		}
		return this;
	}

	public ConnectionSpec getConnectionSpec() {
		return connectionSpec;
	}

	public RestApiConfiguration setConnectionSpec(ConnectionSpec connectionSpec) {
		this.connectionSpec = connectionSpec;
		return this;
	}

	public CertificatePinner getCertificatePinner() {
		return certificatePinner;
	}

	public RestApiConfiguration setCertificatePinner(CertificatePinner certificatePinner) {
		this.certificatePinner = certificatePinner;
		return this;
	}

	public boolean isEnableTls12OnPreLollipop() {
		return enableTls12OnPreLollipop;
	}

	public RestApiConfiguration setEnableTls12OnPreLollipop(boolean enableTls12OnPreLollipop) {
		this.enableTls12OnPreLollipop = enableTls12OnPreLollipop;
		return this;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public RestApiConfiguration setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
		return this;
	}

	public RestApiConfiguration setMockFactory(MockFactory mockFactory) {
		this.mockFactory = mockFactory;
		return this;
	}

	public MockFactory getMockFactory() {
		return mockFactory;
	}

	public void setLogger(RestApiLogger logger) {
		this.logger = logger;
	}

	public RestApiLogger getLogger() {
		return logger;
	}
}
