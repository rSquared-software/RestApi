package software.rsquared.restapi;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class PoolRequest {
	public static final int THREAD_POOL_EXECUTOR = 1;
	public static final int SERIAL_EXECUTOR = 2;

	@IntDef({THREAD_POOL_EXECUTOR, SERIAL_EXECUTOR})
	@Retention(RetentionPolicy.SOURCE)
	public @interface PoolExecutor {
	}

	protected Map<Integer, Request> requestPool = new LinkedHashMap<>();

	protected AtomicBoolean executed = new AtomicBoolean(false);

	protected AtomicBoolean cancelled = new AtomicBoolean(false);

	@Nullable
	private software.rsquared.restapi.listeners.PoolRequestListener listener;

	protected RestApi api;

	protected PoolRequest() {
	}

	public static PoolRequest create(@PoolExecutor int executor) {
		switch (executor) {
			case THREAD_POOL_EXECUTOR:
				return new ThreadPoolRequest();
			case SERIAL_EXECUTOR:
			default:
				return new SerialPoolRequest();
		}
	}

	public PoolRequest addTask(@NonNull Request request, int requestCode) {
		if (executed.get()) {
			throw new IllegalStateException("New task cannot be added to the pool after executing.");
		}
		if (requestPool.containsKey(requestCode)) {
			throw new IllegalArgumentException("Task with this requestCode (" + requestCode + ") was already added.");
		}
		requestPool.put(requestCode, request);
		return this;
	}

	public void execute(RestApi api, @Nullable software.rsquared.restapi.listeners.PoolRequestListener listener) {
		if (executed.compareAndSet(false, true)) {
			this.api = api;
			this.listener = listener;
			execute();
		} else {
			throw new IllegalStateException("Already executed.");
		}
	}

	protected abstract void execute();

	protected void onPreExecute() {
		if (listener != null) {
			api.getUiExecutor().execute(() -> listener.onPreExecute());
		}
	}

	protected void onTaskSuccess(Object result, int requestCode) {
		if (listener != null) {
			listener.onTaskSuccess(result, requestCode);
		}
	}

	protected void onFailed(RequestException e, int requestCode) {
		if (listener != null) {
			api.getUiExecutor().execute(() -> listener.onFailed(e, requestCode));
		}
	}

	protected boolean canContinueAfterFailed(RequestException e, int requestCode) {
		return listener == null || listener.canContinueAfterFailed(e, requestCode);
	}

	protected void onCanceled() {
		if (listener != null) {
			api.getUiExecutor().execute(() -> listener.onCanceled());
		}
	}

	protected void onSuccess(Map<Integer, Object> results) {
		if (listener != null) {
			api.getUiExecutor().execute(() -> listener.onSuccess(results));
		}
	}

	protected void onPostExecute() {
		if (listener != null) {
			api.getUiExecutor().execute(() -> listener.onPostExecute());
		}
	}

	public void cancel() {
		if (cancelled.compareAndSet(false, true)){
			for (Request request : requestPool.values()) {
				request.cancel();
			}
			onCanceled();
		}
	}


	protected abstract class PoolRequestListener implements RequestListener<Object> {
		private int requestCode;

		public PoolRequestListener(int requestCode) {
			this.requestCode = requestCode;
		}

		public int getRequestCode() {
			return requestCode;
		}
	}

}
