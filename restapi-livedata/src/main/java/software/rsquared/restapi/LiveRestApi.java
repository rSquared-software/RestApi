package software.rsquared.restapi;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import software.rsquared.restapi.exceptions.RequestException;
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
public class LiveRestApi {

	final RestApi api;

	LiveRestApi(RestApi api) {
		this.api = api;
	}

	@NonNull
	public <T> LiveData<ApiResource<T>> execute(@NonNull Request<T> request) {
		ApiResourceLiveData<T> liveData = new ApiResourceLiveData<>();
		request.execute(api, new RequestListener<T>() {

			@Override
			public void onPreExecute() {
				liveData.setValue(ApiResource.loading());
			}

			@Override
			public void onSuccess(T result) {
				liveData.setValue(ApiResource.success(result));

			}

			@Override
			public void onFailed(RequestException e) {
				liveData.setValue(ApiResource.fail(e));
			}

			@Override
			public void onPostExecute() {

			}

			@Override
			public void onCanceled() {
				liveData.setValue(ApiResource.cancel());

			}
		});
		return liveData;
	}

	@NonNull
	public <T> LiveData<ApiResource<T>> execute(@NonNull Request<T> request, RequestListener<T> listener) {
		ApiResourceLiveData<T> liveData = new ApiResourceLiveData<>();
		request.execute(api, new RequestListener<T>() {

			@Override
			public void onPreExecute() {
				liveData.setValue(ApiResource.loading());
				if (listener != null) {
					listener.onPreExecute();
				}
			}

			@Override
			public void onSuccess(T result) {
				liveData.setValue(ApiResource.success(result));
				if (listener != null) {
					listener.onSuccess(result);
				}
			}

			@Override
			public void onFailed(RequestException e) {
				liveData.setValue(ApiResource.fail(e));
				if (listener != null) {
					listener.onFailed(e);
				}
			}

			@Override
			public void onPostExecute() {
				if (listener != null) {
					listener.onPostExecute();
				}
			}

			@Override
			public void onCanceled() {
				liveData.setValue(ApiResource.cancel());
				if (listener != null) {
					listener.onCanceled();
				}
			}
		});
		return liveData;
	}

	@WorkerThread
	public <E> E executeSync(@NonNull Request<E> request) throws RequestException {
		return request.executeSync(api);
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


		public Builder(@NonNull String url) {
			this.url = url;
			this.successStatusCodes.add(200);
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

		public LiveRestApi build() {
			Executor uiExecutor = this.uiExecutor;
			if (uiExecutor == null) {
				uiExecutor = RestApi.DEFAULT_MAIN_THREAD_EXECUTOR;
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


			return new LiveRestApi(new RestApi(timeout, url, basicAuth, enableTls12OnPreLollipop, networkExecutor,
					uiExecutor, errorDeserializer, deserializer, serializer, checkerList,
					requestAuthenticator, mockFactory, headerFactory, logger, successStatusCodes));
		}

	}

}
