package software.rsquared.restapi;

import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.ErrorCallback;
import software.rsquared.restapi.listeners.PoolRequestListener;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonDeserializer;
import software.rsquared.restapi.serialization.JsonErrorDeserializer;
import software.rsquared.restapi.serialization.ObjectToFormSerializer;
import software.rsquared.restapi.serialization.Serializer;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public class RestApi {

	static final MainThreadExecutor DEFAULT_MAIN_THREAD_EXECUTOR = new MainThreadExecutor();

	private final long timeout;

	@NonNull
	private final String url;

	@Nullable
	private final BasicAuth basicAuth;

	private final boolean enableTls12OnPreLollipop;

	@Nullable
	private final Executor networkExecutor;

	@NonNull
	private final Executor uiExecutor;

	@NonNull
	private final ErrorDeserializer errorDeserializer;

	@NonNull
	private final Deserializer deserializer;

	@NonNull
	private final Serializer serializer;

	@NonNull
	private final List<Checker> checkerList;

	@Nullable
	private final RequestAuthenticator requestAuthenticator;

	@Nullable
	private final MockFactory mockFactory;

	@Nullable
	private final HeaderFactory headerFactory;

	@NonNull
	private final RestApiLogger logger;

	@NonNull
	private final Set<Integer> successStatusCodes;

	@Nullable
	private final ErrorCallback errorCallback;

	RestApi(long timeout, @NonNull String url, @Nullable BasicAuth basicAuth,
	        boolean enableTls12OnPreLollipop, @Nullable Executor networkExecutor,
	        @NonNull Executor uiExecutor, @NonNull ErrorDeserializer errorDeserializer,
	        @NonNull Deserializer deserializer, @NonNull Serializer serializer,
	        @NonNull List<Checker> checkerList, @Nullable RequestAuthenticator requestAuthenticator,
	        @Nullable MockFactory mockFactory, @Nullable HeaderFactory headerFactory,
	        @NonNull RestApiLogger logger, @NonNull Set<Integer> successStatusCodes, @Nullable ErrorCallback errorCallback) {
		this.timeout = timeout;
		this.url = url;
		this.basicAuth = basicAuth;
		this.enableTls12OnPreLollipop = enableTls12OnPreLollipop;
		this.networkExecutor = networkExecutor;
		this.uiExecutor = uiExecutor;
		this.errorDeserializer = errorDeserializer;
		this.deserializer = deserializer;
		this.serializer = serializer;
		this.checkerList = checkerList;
		this.requestAuthenticator = requestAuthenticator;
		this.mockFactory = mockFactory;
		this.headerFactory = headerFactory;
		this.logger = logger;
		this.successStatusCodes = successStatusCodes;
		this.errorCallback = errorCallback;
	}

	@AnyThread
	public <E> void execute(@NonNull Request<E> request, @Nullable RequestListener<E> listener) {
		request.execute(this, listener);
	}

	@AnyThread
	public void execute(@NonNull PoolRequest request, @Nullable PoolRequestListener listener) {
		request.execute(this, listener);
	}

	@WorkerThread
	public <E> E executeSync(@NonNull Request<E> request) throws RequestException {
		return request.executeSync(this);
	}

	@NonNull
	Executor getUiExecutor() {
		return uiExecutor;
	}

	@Nullable
	Executor getNetworkExecutor() {
		return networkExecutor;
	}

	@NonNull
	String getUrl() {
		return url;
	}

	@Nullable
	RequestAuthenticator getRequestAuthenticator() {
		return requestAuthenticator;
	}

	@NonNull
	RestApiLogger getLogger() {
		return logger;
	}

	@NonNull
	Set<Integer> getSuccessStatusCodes() {
		return successStatusCodes;
	}

	long getTimeout() {
		return timeout;
	}

	@Nullable
	BasicAuth getBasicAuth() {
		return basicAuth;
	}

	@NonNull
	ErrorDeserializer getErrorDeserializer() {
		return errorDeserializer;
	}

	@NonNull
	Deserializer getDeserializer() {
		return deserializer;
	}

	@NonNull
	Serializer getSerializer() {
		return serializer;
	}

	@Nullable
	MockFactory getMockFactory() {
		return mockFactory;
	}

	boolean isEnableTls12OnPreLollipop() {
		return enableTls12OnPreLollipop;
	}

	@Nullable
	HeaderFactory getHeaderFactory() {
		return headerFactory;
	}

	@NonNull
	List<Checker> getCheckerList() {
		return checkerList;
	}

	@Nullable
	public ErrorCallback getErrorCallback() {
		return errorCallback;
	}

	public static class Builder {

		private long timeout = 60_000;
		@NonNull
		private String url;
		@Nullable
		private BasicAuth basicAuth;
		private boolean enableTls12OnPreLollipop;
		@Nullable
		private Executor networkExecutor;
		@Nullable
		private Executor uiExecutor;
		@Nullable
		private ErrorDeserializer errorDeserializer;
		@Nullable
		private Deserializer deserializer;
		@Nullable
		private Serializer serializer;
		@NonNull
		private List<Checker> checkerList = new ArrayList<>();
		@Nullable
		private RequestAuthenticator requestAuthenticator;
		@Nullable
		private MockFactory mockFactory;
		@Nullable
		private HeaderFactory headerFactory;
		@Nullable
		private RestApiLogger logger;
		@NonNull
		private Set<Integer> successStatusCodes = new LinkedHashSet<>();
		@Nullable
		private ErrorCallback errorCallback;


		public Builder(@NonNull String url) {
			this.url = url;
			this.successStatusCodes.add(200);
		}

		public Builder setUrl(@NonNull String url) {
			this.url = url;
			return this;
		}

		public Builder setTimeout(long timeout) {
			this.timeout = timeout;
			return this;
		}

		public Builder setBasicAuth(@NonNull BasicAuth basicAuth) {
			this.basicAuth = basicAuth;
			return this;
		}

		public Builder setEnableTls12OnPreLollipop(boolean enableTls12OnPreLollipop) {
			this.enableTls12OnPreLollipop = enableTls12OnPreLollipop;
			return this;
		}

		public Builder setNetworkExecutor(@NonNull Executor networkExecutor) {
			this.networkExecutor = networkExecutor;
			return this;
		}

		public Builder setUiExecutor(@NonNull Executor uiExecutor) {
			this.uiExecutor = uiExecutor;
			return this;
		}

		public Builder setErrorDeserializer(@NonNull ErrorDeserializer errorDeserializer) {
			this.errorDeserializer = errorDeserializer;
			return this;
		}

		public Builder setDeserializer(@NonNull Deserializer deserializer) {
			this.deserializer = deserializer;
			return this;
		}

		public Builder setSerializer(@NonNull Serializer serializer) {
			this.serializer = serializer;
			return this;
		}

		public Builder addChecker(@NonNull Checker checkerList) {
			this.checkerList.add(checkerList);
			return this;
		}

		public Builder setRequestAuthenticator(@NonNull RequestAuthenticator requestAuthenticator) {
			this.requestAuthenticator = requestAuthenticator;
			return this;
		}

		public Builder setMockFactory(@NonNull MockFactory mockFactory) {
			this.mockFactory = mockFactory;
			return this;
		}

		public Builder setHeaderFactory(@NonNull HeaderFactory headerFactory) {
			this.headerFactory = headerFactory;
			return this;
		}

		public Builder setLogger(@NonNull RestApiLogger logger) {
			this.logger = logger;
			return this;
		}

		public Builder addSuccessStatusCodes(int... codes) {
			for (int code : codes) {
				this.successStatusCodes.add(code);
			}
			return this;
		}

		public Builder setErrorCallback(@Nullable ErrorCallback errorCallback) {
			this.errorCallback = errorCallback;
			return this;
		}

		public RestApi build() {
			Executor uiExecutor = this.uiExecutor;
			if (uiExecutor == null) {
				uiExecutor = DEFAULT_MAIN_THREAD_EXECUTOR;
			}
			ErrorDeserializer errorDeserializer = this.errorDeserializer;
			if (errorDeserializer == null) {
				errorDeserializer = new JsonErrorDeserializer();
			}
			Deserializer deserializer = this.deserializer;
			if (deserializer == null) {
				deserializer = new JsonDeserializer();
			}
			Serializer serializer = this.serializer;
			if (serializer == null) {
				serializer = new ObjectToFormSerializer();
			}
			RestApiLogger logger = this.logger;
			if (logger == null) {
				logger = RestApiLoggerFactory.create();
			}


			return new RestApi(timeout, url, basicAuth, enableTls12OnPreLollipop, networkExecutor,
					uiExecutor, errorDeserializer, deserializer, serializer, checkerList,
					requestAuthenticator, mockFactory, headerFactory, logger, successStatusCodes, errorCallback);
		}

	}

}
