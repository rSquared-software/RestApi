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

	protected Map<Integer, Request> mRequestPool = new LinkedHashMap<>();

	protected boolean mExecuted;

	protected RequestExecutor mExecutor;

	protected PoolRequest(int poolSize){
		mExecutor = new RequestExecutor(poolSize, 0L);
	}

	public P addTask(@NonNull Request request, int requestCode) {
		if (mExecuted) {
			throw new IllegalStateException("New task cannot be added to the pool after executing.");
		}
		if (mRequestPool.containsKey(requestCode)) {
			throw new IllegalArgumentException("Task with this requestCode (" + requestCode + ") was already added.");
		}
		mRequestPool.put(requestCode, request);
		//noinspection unchecked
		return (P) this;
	}

	public abstract void execute(RequestPoolListener listener);

	public void stopExecute() {
		mExecutor.shutdownNow();
	}

	abstract class PoolRequestListener extends RequestListener {
		private int mRequestCode;

		public PoolRequestListener(int requestCode) {
			mRequestCode = requestCode;
		}

		public int getRequestCode() {
			return mRequestCode;
		}
	}

}
