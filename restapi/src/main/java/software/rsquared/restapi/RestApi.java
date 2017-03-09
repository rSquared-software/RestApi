package software.rsquared.restapi;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import software.rsquared.restapi.exceptions.DefaultErrorResponse;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.ErrorCallback;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.listeners.RequestPoolListener;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonSerializer;
import software.rsquared.restapi.serialization.JsonDeserializer;
import software.rsquared.restapi.serialization.JsonErrorDeserializer;
import software.rsquared.restapi.serialization.Serializer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.androidlogger.Level;
import software.rsquared.androidlogger.Logger;
import software.rsquared.androidlogger.logcat.LogcatLogger;
import software.rsquared.androidlogger.logcat.LogcatLoggerConfig;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestApi {

    public static final int THREAD_POOL_EXECUTOR = 1;
    public static final int SERIAL_EXECUTOR = 2;
    private static Config sConfiguration;

    static Config getConfiguration() {
        return sConfiguration;
    }

    /**
     * Set configuration of the connection to the api. This method must be called before using any connection with the Api
     *
     * @param configuration object with rest configuration
     */
    public static void setConfiguration(Config configuration) {
        sConfiguration = configuration;
    }

    static Logger getLogger() {
        return sConfiguration.logger;
    }

    static boolean isConfigured() {
        return sConfiguration != null;
    }

    public static <E> void execute(Request<E> request, RequestListener<E> listener) {
        request.execute(listener);
    }

    public static <E> E executeSync(Request<E> request) throws RequestException {
        return request.execute().get();
    }

    public static PoolBuilder pool(@Executor int executor) {
        return new PoolBuilder(executor);
    }

    @IntDef({THREAD_POOL_EXECUTOR, SERIAL_EXECUTOR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Executor {
    }

    public static class PoolBuilder {
        protected Map<Integer, Request> mRequestPool = new LinkedHashMap<>();
        @Executor
        private int mExecutor;

        public PoolBuilder(@Executor int executor) {
            mExecutor = executor;
        }

        public PoolRequest build() {
            PoolRequest poolRequest;
            switch (mExecutor) {
                case THREAD_POOL_EXECUTOR:
                    poolRequest = new ThreadPoolRequest(mRequestPool.size());
                    break;
                case SERIAL_EXECUTOR:
                default:
                    poolRequest = new SerialPoolRequest();
                    break;

            }
            for (Map.Entry<Integer, Request> entry : mRequestPool.entrySet()) {
                poolRequest.addTask(entry.getValue(), entry.getKey());
            }
            return poolRequest;
        }

        public void execute(RequestPoolListener listener) {
            PoolRequest poolRequest = build();
            poolRequest.execute(listener);
        }


        public PoolBuilder add(@NonNull Request request, int requestCode) {
            mRequestPool.put(requestCode, request);
            return this;
        }
    }

    public static class Config {

        public static final String HTTP = "http";
        public static final String HTTPS = "https";

        private int timeout = 60 * 1000;
        private String scheme = HTTP;
        private String host;
        private int port = -1;
        @Nullable
        private BasicAuthorization basicAuthorization;
        @Nullable
        private InitialRequirements initialRequirements;

        private Logger logger = new LogcatLogger();

        @NonNull
        private ErrorDeserializer errorDeserializer = new JsonErrorDeserializer();

        @NonNull
        private Deserializer deserializer = new JsonDeserializer();

        @NonNull
        private Serializer serializer = new JsonSerializer();

        private RestAuthorizationService userService;

        private Class<? extends DefaultErrorResponse> errorResponseClass;

        private RestAuthorizationService restAuthorizationService;

        private Map<String, String> headers = new HashMap<>();

        private ErrorCallback errorCallback;

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
        public Config setTimeout(int timeout) {
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
        public Config setScheme(@NonNull String scheme) {
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
        public Config setHost(@NonNull String host) {
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
        public Config setPort(int port) {
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
        public Config setAuthorization(@NonNull String user, @NonNull String password) {
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
        public Config setInitialRequirements(@NonNull InitialRequirements initialRequirements) {
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
        public Config setDeserializer(@NonNull Deserializer deserializer) {
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
        public Config setSerializer(@NonNull Serializer serializer) {
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
        public Config setRestAuthorizationService(RestAuthorizationService authorizationService) {
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
        public Config setErrorDeserializer(@NonNull ErrorDeserializer errorDeserializer) {
            this.errorDeserializer = errorDeserializer;
            return this;
        }

        /**
         * Requests log {@link Level}
         * <p>
         * default: {@link Level#VERBOSE}
         */
        public Config setLogLevel(Level level) {
            ((LogcatLogger) logger).setConfig(new LogcatLoggerConfig().setLevel(level));
            return this;
        }

        public Config addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Config removeHeader(String name) {
            headers.remove(name);
            return this;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public ErrorCallback getErrorCallback() {
            return errorCallback;
        }

        public Config setErrorCallback(ErrorCallback errorCallback) {
            this.errorCallback = errorCallback;
            return this;
        }
    }
}
