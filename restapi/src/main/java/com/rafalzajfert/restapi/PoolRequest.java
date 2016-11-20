package com.rafalzajfert.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rafalzajfert.restapi.listeners.ResponseListener;
import com.rafalzajfert.restapi.listeners.ResponsePoolListener;

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

	public abstract void execute(ResponsePoolListener listener);

	public void stopExecute() {
		mExecutor.shutdownNow();
	}

	abstract class PoolResponseListener implements ResponseListener{
		private int mRequestCode;

		public PoolResponseListener(int requestCode) {
			mRequestCode = requestCode;
		}

		public int getRequestCode() {
			return mRequestCode;
		}
	}

}
