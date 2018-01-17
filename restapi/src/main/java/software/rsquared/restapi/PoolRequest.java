package software.rsquared.restapi;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.listeners.RequestPoolListener;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
abstract class PoolRequest<P extends PoolRequest> {

	protected Map<Integer, Request> requestPool = new LinkedHashMap<>();

	protected boolean executed;

	protected RequestExecutor executor;

	protected boolean cancelled;

	@Nullable
	private RequestPoolListener listener;
	private Handler handler;

	protected PoolRequest(int poolSize) {
		executor = new RequestExecutor(poolSize, 0L);
	}

	public P addTask(@NonNull Request request, int requestCode) {
		if (executed) {
			throw new IllegalStateException("New task cannot be added to the pool after executing.");
		}
		if (requestPool.containsKey(requestCode)) {
			throw new IllegalArgumentException("Task with this requestCode (" + requestCode + ") was already added.");
		}
		requestPool.put(requestCode, request);
		//noinspection unchecked
		return (P) this;
	}

	@CallSuper
	public void execute(@Nullable RequestPoolListener listener) {
		this.listener = listener;
		execute();
	}

	public abstract void execute();

	protected void onPreExecute() {
		if (listener != null) {
			getHandler().post(() -> listener.onPreExecute());
		}
	}

	protected void onTaskSuccess(Object result, int requestCode) {
		if (listener != null) {
			getHandler().post(() -> listener.onTaskSuccess(result, requestCode));
		}
	}

	protected void onFailed(RequestException e, int requestCode) {
		if (listener != null) {
			getHandler().post(() -> listener.onFailed(e, requestCode));
		}
	}

	protected boolean canContinueAfterFailed(RequestException e, int requestCode) {
		return listener == null || listener.canContinueAfterFailed(e, requestCode);
	}

	protected void onCanceled() {
		if (listener != null) {
			getHandler().post(() -> listener.onCanceled());
		}
	}

	protected void onSuccess(Map<Integer, Object> results) {
		if (listener != null) {
			getHandler().post(() -> listener.onSuccess(results));
		}
	}

	protected void onPostExecute() {
		if (listener != null) {
			getHandler().post(() -> listener.onPostExecute());
		}
	}

	public void cancel() {
		stopExecute();
	}

	protected void stopExecute() {
		executor.shutdownNow();
	}

	/**
	 * Get handler for the main looper
	 */
	@NonNull
	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler(Looper.getMainLooper());
		}
		return handler;
	}

	abstract class PoolRequestListener implements RequestListener<Object> {
		private int requestCode;

		public PoolRequestListener(int requestCode) {
			this.requestCode = requestCode;
		}

		public int getRequestCode() {
			return requestCode;
		}
	}

}
