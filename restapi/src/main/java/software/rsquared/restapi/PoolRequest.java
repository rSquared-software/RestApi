package software.rsquared.restapi;

import android.support.annotation.NonNull;

import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.listeners.RequestPoolListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
abstract class PoolRequest<P extends PoolRequest> {

	protected Map<Integer, Request> requestPool = new LinkedHashMap<>();

	protected boolean executed;

	protected RequestExecutor executor;

	protected boolean cancelled;

	protected PoolRequest(int poolSize){
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

	public abstract void execute(RequestPoolListener listener);

	public void cancel() {
		stopExecute();
	}

	protected void stopExecute() {
		executor.shutdownNow();
	}

	abstract class PoolRequestListener extends RequestListener {
		private int requestCode;

		public PoolRequestListener(int requestCode) {
			this.requestCode = requestCode;
		}

		public int getRequestCode() {
			return requestCode;
		}
	}

}
