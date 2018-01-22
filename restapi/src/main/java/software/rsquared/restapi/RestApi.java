package software.rsquared.restapi;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.listeners.RequestPoolListener;
import software.rsquared.restapi.listeners.SyncRequestListener;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class RestApi {
	public static final int THREAD_POOL_EXECUTOR = 1;
	public static final int SERIAL_EXECUTOR = 2;
	private static RestApiConfiguration configuration;
	@NonNull
	private static SparseArray<RequestFuture> requests = new SparseArray<>();
	@NonNull
	private static SparseArray<PoolRequest> poolRequests = new SparseArray<>();

	static RestApiConfiguration getConfiguration() {
		return configuration;
	}

	/**
	 * Set configuration of the connection to the api. This method must be called before using any connection with the Api
	 *
	 * @param configuration object with rest configuration
	 */
	public static void setConfiguration(@NonNull RestApiConfiguration configuration) {
		RestApi.configuration = configuration;
	}

	public static <E> void execute(@NonNull Request<E> request, @Nullable RequestListener<E> listener) {
		request.execute(listener);
	}

	public static <E> E executeSync(@NonNull Request<E> request) throws RequestException {
		return request.execute().get();
	}

	@Nullable
	public static <E> E executeSync(@NonNull Request<E> request, @Nullable SyncRequestListener<E> listener) {
		try {
			return request.execute(listener).get();
		} catch (RequestException e) {
			//exceptions should be caught in listener
			return null;
		}
	}

	public static <E> void execute(@NonNull final Request<E> request, final int requestCode, @Nullable final RequestListener<E> listener) {
		RequestFuture<E> future = request.execute(new RequestListener<E>() {
			@Override
			public void onSuccess(E result) {
				if (listener != null) {
					listener.onSuccess(result);
				}
			}

			@Override
			public void onFailed(RequestException e) {
				if (listener != null) {
					listener.onFailed(e);
				}
			}

			@Override
			public void onPreExecute() {
				if (listener != null) {
					listener.onPreExecute();
				}
			}

			@Override
			public void onPostExecute() {
				requests.delete(requestCode);
				if (listener != null) {
					listener.onPostExecute();
				}
			}

			@Override
			public void onCanceled() {
				if (listener != null) {
					listener.onCanceled();
				}
			}
		});
		requests.put(requestCode, future);
	}

	public static PoolBuilder pool(@Executor int executor) {
		return new PoolBuilder(executor);
	}

	public static void cancel(int requestCode) {
		RequestFuture future = requests.get(requestCode);
		if (future != null) {
			future.cancel(true);
		}
	}

	public static void cancelPool(int requestCode) {
		PoolRequest pool = poolRequests.get(requestCode);
		if (pool != null) {
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

		public void execute(@Nullable RequestPoolListener listener) {
			PoolRequest poolRequest = build();
			poolRequest.execute(listener);
		}

		public void execute(@Nullable final RequestPoolListener listener, final int requestCode) {
			final PoolRequest poolRequest = build();
			poolRequest.execute(new RequestPoolListener() {
				@Override
				public void onSuccess(@NonNull Map<Integer, Object> result) {
					if (listener != null) {
						listener.onSuccess(result);
					}
				}

				@Override
				public void onFailed(RequestException e, int requestCode) {
					if (listener != null) {
						listener.onFailed(e, requestCode);
					}
				}

				@Override
				public boolean canContinueAfterFailed(RequestException e, int requestCode) {
					return listener == null || listener.canContinueAfterFailed(e, requestCode);
				}

				@Override
				public void onPreExecute() {
					if (listener != null) {
						listener.onPreExecute();
					}
				}

				@Override
				public void onTaskSuccess(Object result, int requestCode) {
					if (listener != null) {
						listener.onTaskSuccess(result, requestCode);
					}
				}

				@Override
				public void onPostExecute() {
					if (listener != null) {
						listener.onPostExecute();
					}
					poolRequests.delete(requestCode);
				}

				@Override
				public void onCanceled() {
					if (listener != null) {
						listener.onCanceled();
					}
				}
			});
			poolRequests.put(requestCode, poolRequest);
		}


		public PoolBuilder add(@NonNull Request request, int requestCode) {
			requestPool.put(requestCode, request);
			return this;
		}
	}

}
