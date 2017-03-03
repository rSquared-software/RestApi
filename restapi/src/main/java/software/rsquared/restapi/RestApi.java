package software.rsquared.restapi;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import software.rsquared.restapi.exceptions.DefaultErrorResponse;
import software.rsquared.restapi.exceptions.RequestException;
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
        return sConfiguration.mLogger;
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

        private int mTimeout = 60 * 1000;
        private String mScheme = HTTP;
        private String mHost;
        private int mPort = -1;
        @Nullable
        private BasicAuthorization mBasicAuthorization;
        @Nullable
        private InitialRequirements mInitialRequirements;

        private Logger mLogger = new LogcatLogger();

        @NonNull
        private ErrorDeserializer mErrorDeserializer = new JsonErrorDeserializer();

        @NonNull
        private Deserializer mDeserializer = new JsonDeserializer();

        @NonNull
        private Serializer mSerializer = new JsonSerializer();

        private RestAuthorizationService mUserService;

        private Class<? extends DefaultErrorResponse> mErrorResponseClass;

        private RestAuthorizationService mRestAuthorizationService;

        private Map<String, String> mHeaders = new HashMap<>();

        /**
         * Timeout for the connections.
         * A value of 0 means no timeout, otherwise values must be between 1 and Integer.MAX_VALUE milliseconds.<p>
         * default: 1min
         */
        public int getTimeout() {
            return mTimeout;
        }

        /**
         * Sets the default connect timeout for the connections.
         * A value of 0 means no timeout, otherwise values must be between 1 and Integer.MAX_VALUE milliseconds.<p>
         * default: 1min
         */
        public Config setTimeout(int timeout) {
            mTimeout = timeout;
            return this;
        }

        /**
         * Url scheme - http or https
         * <p>
         * default: http
         */
        public String getScheme() {
            return mScheme;
        }

        /**
         * Sets the default url scheme - http or https
         * <p>
         * default: http
         */
        public Config setScheme(@NonNull String scheme) {
            mScheme = scheme;
            return this;
        }

        /**
         * Rest Api host - either a regular hostname, International Domain Name, IPv4 address, or IPv6
         * address.
         */
        public String getHost() {
            return mHost;
        }

        /**
         * Rest Api host - either a regular hostname, International Domain Name, IPv4 address, or IPv6
         * address.
         */
        public Config setHost(@NonNull String host) {
            mHost = host;
            return this;
        }

        /**
         * Port for communication with rest api
         * <p>
         * default: 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
         * otherwise.
         */
        public int getPort() {
            return mPort;
        }

        /**
         * Port for communication with rest api
         * <p>
         * default: 80 if {@code scheme.equals("http")}, 443 if {@code scheme.equals("https")} and -1
         * otherwise.
         */
        public Config setPort(int port) {
            mPort = port;
            return this;
        }

        /**
         * Basic authorization for all requests
         */
        @Nullable
        public BasicAuthorization getBasicAuthorization() {
            return mBasicAuthorization;
        }

        /**
         * Sets basic authorization for all requests
         */
        public Config setAuthorization(@NonNull String user, @NonNull String password) {
            if (TextUtils.isEmpty(user) || TextUtils.isEmpty(password)) {
                mBasicAuthorization = null;
                return this;
            }
            mBasicAuthorization = new BasicAuthorization(user, password);
            return this;
        }

        /**
         * Initial restrictions for all requests
         */
        @Nullable
        public InitialRequirements getInitialRequirements() {
            return mInitialRequirements;
        }

        /**
         * Initial restrictions for all requests
         */
        public Config setInitialRequirements(@NonNull InitialRequirements initialRequirements) {
            mInitialRequirements = initialRequirements;
            return this;
        }

        /**
         * Response deserializer
         * <p>
         * default: {@link JsonDeserializer JsonDeserializer}
         */
        @NonNull
        public Deserializer getDeserializer() {
            return mDeserializer;
        }


        /**
         * Response deserializer
         * <p>
         * default: {@link JsonDeserializer JsonDeserializer}
         */
        public Config setDeserializer(@NonNull Deserializer deserializer) {
            mDeserializer = deserializer;
            return this;
        }


        /**
         * Request parameters serializer
         * <p>
         * default: {@link JsonSerializer JsonSerializer}
         */
        @NonNull
        public Serializer getSerializer() {
            return mSerializer;
        }


        /**
         * Request parameters serializer
         * <p>
         * default: {@link JsonSerializer JsonSerializer}
         */
        public Config setSerializer(@NonNull Serializer serializer) {
            mSerializer = serializer;
            return this;
        }

        /**
         * Authorization service
         * <p>
         * default: null
         */
        @Nullable
        public RestAuthorizationService getRestAuthorizationService() {
            return mRestAuthorizationService;
        }

        /**
         * Authorization service
         * <p>
         * default: null
         */
        public Config setRestAuthorizationService(RestAuthorizationService authorizationService) {
            mRestAuthorizationService = authorizationService;
            return this;
        }

        /**
         * Error response deserializer
         * <p>
         * default: {@link JsonErrorDeserializer JsonErrorDeserializer}
         */
        @NonNull
        public ErrorDeserializer getErrorDeserializer() {
            return mErrorDeserializer;
        }

        /**
         * Error response deserializer
         * <p>
         * default: {@link JsonErrorDeserializer JsonErrorDeserializer}
         */
        public Config setErrorDeserializer(@NonNull ErrorDeserializer errorDeserializer) {
            mErrorDeserializer = errorDeserializer;
            return this;
        }

        /**
         * Requests log {@link Level}
         * <p>
         * default: {@link Level#VERBOSE}
         */
        public Config setLogLevel(Level level) {
            ((LogcatLogger) mLogger).setConfig(new LogcatLoggerConfig().setLevel(level));
            return this;
        }

        public Config addHeader(String name, String value) {
            mHeaders.put(name, value);
            return this;
        }

        public Config removeHeader(String name) {
            mHeaders.remove(name);
            return this;
        }

        public Map<String, String> getHeaders() {
            return mHeaders;
        }
    }
}
