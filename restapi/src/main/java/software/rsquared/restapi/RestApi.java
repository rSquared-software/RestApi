package software.rsquared.restapi;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import software.rsquared.androidlogger.Level;
import software.rsquared.androidlogger.Logger;
import software.rsquared.androidlogger.logcat.LogcatLogger;
import software.rsquared.androidlogger.logcat.LogcatLoggerConfig;
import software.rsquared.restapi.exceptions.DefaultErrorResponse;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.ErrorCallback;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.listeners.RequestPoolListener;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonDeserializer;
import software.rsquared.restapi.serialization.JsonErrorDeserializer;
import software.rsquared.restapi.serialization.JsonSerializer;
import software.rsquared.restapi.serialization.ObjectToFormSerializer;
import software.rsquared.restapi.serialization.Serializer;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestApi {

    public static final int THREAD_POOL_EXECUTOR = 1;
    public static final int SERIAL_EXECUTOR = 2;
    private static Config configuration;
    private static SparseArray<RequestFuture> requests = new SparseArray<>();
    private static SparseArray<PoolRequest> poolRequests = new SparseArray<>();

    static Config getConfiguration() {
        return configuration;
    }

    /**
     * Set configuration of the connection to the api. This method must be called before using any connection with the Api
     *
     * @param configuration object with rest configuration
     */
    public static void setConfiguration(Config configuration) {
        RestApi.configuration = configuration;
    }

    static Logger getLogger() {
        return configuration.logger;
    }

    static boolean isConfigured() {
        return configuration != null;
    }

    public static <E> void execute(Request<E> request, RequestListener<E> listener) {
        request.execute(listener);
    }

    public static <E> E executeSync(Request<E> request) throws RequestException {
        return request.execute().get();
    }

    public static <E> void execute(final Request<E> request, final int requestCode, final RequestListener<E> listener) {
        RequestFuture<E> future = request.execute(new RequestListener<E>() {
            @Override
            public void onSuccess(E result) {
                listener.onSuccess(result);
            }

            @Override
            public void onFailed(RequestException e) {
                listener.onFailed(e);
            }

            @Override
            public void onPreExecute() {
                listener.onPreExecute();
            }

            @Override
            public void onPostExecute() {
                requests.delete(requestCode);
                listener.onPostExecute();
            }

            @Override
            public void onCancel() {
                listener.onCancel();
            }
        });
        requests.put(requestCode, future);
    }

    public static PoolBuilder pool(@Executor int executor) {
        return new PoolBuilder(executor);
    }

    public static void cancel(int requestCode){
        RequestFuture future = requests.get(requestCode);
        if (future != null){
            future.cancel(true);
        }
    }

    public static void cancelPool(int requestCode){
        PoolRequest pool = poolRequests.get(requestCode);
        if (pool != null){
            pool.cancel();
        }
    }

    @IntDef({THREAD_POOL_EXECUTOR, SERIAL_EXECUTOR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Executor {
    }

    public static class PoolBuilder {
        protected Map<Integer, Request> requestPool = new LinkedHashMap<>();
        @Executor
        private int executor;

        public PoolBuilder(@Executor int executor) {
            this.executor = executor;
        }

        public PoolRequest build() {
            PoolRequest poolRequest;
            switch (executor) {
                case THREAD_POOL_EXECUTOR:
                    poolRequest = new ThreadPoolRequest(requestPool.size());
                    break;
                case SERIAL_EXECUTOR:
                default:
                    poolRequest = new SerialPoolRequest();
                    break;
            }
            for (Map.Entry<Integer, Request> entry : requestPool.entrySet()) {
                poolRequest.addTask(entry.getValue(), entry.getKey());
            }
            return poolRequest;
        }

        public void execute(RequestPoolListener listener) {
            PoolRequest poolRequest = build();
            poolRequest.execute(listener);
        }

        public void execute(final RequestPoolListener listener, final int requestCode) {
            final PoolRequest poolRequest = build();
            poolRequest.execute(new RequestPoolListener() {
                @Override
                public void onSuccess(@NonNull Map<Integer, Object> result) {
                    listener.onSuccess(result);
                }

                @Override
                public boolean onFailed(RequestException e, int requestCode) {
                    return listener.onFailed(e, requestCode);
                }

                @Override
                public void onPreExecute() {
                    listener.onPreExecute();
                }

                @Override
                public void onTaskSuccess(Object result, int requestCode) {
                    listener.onTaskSuccess(result, requestCode);
                }

                @Override
                public void onPostExecute() {
                    listener.onPostExecute();
                    poolRequests.delete(requestCode);
                }

                @Override
                public void onCancel() {
                    listener.onCancel();
                }
            });
            poolRequests.put(requestCode, poolRequest);
        }


        public PoolBuilder add(@NonNull Request request, int requestCode) {
            requestPool.put(requestCode, request);
            return this;
        }
    }

    public static class Config {

        public static final String HTTP = "http";
        public static final String HTTPS = "https";

        private Set<Integer> successStatusCodes = new HashSet<>(Collections.singletonList(200));
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
        private Serializer serializer = new ObjectToFormSerializer();

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
        public Config setLogLevel(@NonNull Level level) {
            ((LogcatLogger) logger).setConfig(new LogcatLoggerConfig().setLevel(level));
            return this;
        }

        /**
         * Add header to all requests
         */
        public Config addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * remove header added by {@link #addHeader(String, String)}
         */
        public Config removeHeader(String name) {
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


        public Config setErrorCallback(@Nullable ErrorCallback errorCallback) {
            this.errorCallback = errorCallback;
            return this;
        }

        public Set<Integer> getSuccessStatusCodes() {
            return successStatusCodes;
        }

        public void setSuccessStatusCodes(Set<Integer> successStatusCodes) {
            this.successStatusCodes = successStatusCodes;
        }
    }
}
