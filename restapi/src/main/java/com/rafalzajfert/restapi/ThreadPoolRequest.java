package com.rafalzajfert.restapi;

import com.rafalzajfert.restapi.exceptions.RequestException;
import com.rafalzajfert.restapi.listeners.ResponsePoolListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class ThreadPoolRequest extends PoolRequest<ThreadPoolRequest> {

    private Map<Integer, Object> mResults = new LinkedHashMap<>();
    private ResponsePoolListener mListener;

    public ThreadPoolRequest(int poolSize) {
        super(poolSize);
    }

    public void execute(ResponsePoolListener listener) {
        if (mExecuted) {
            throw new IllegalStateException("Already executed.");
        }
        mExecuted = true;

        mListener = listener;
        for (Map.Entry<Integer, Request> entry : mRequestPool.entrySet()) {
            mExecutor.submit(entry.getValue().createRequestTask(), new PoolRequest.PoolResponseListener(entry.getKey()) {
                @Override
                public void onSuccess(Object result) {
                    int requestCode = getRequestCode();
                    mResults.put(requestCode, result);
                    if (mListener != null) {
                        mListener.onTaskSuccess(result, requestCode);
                    }
                    checkFinished();
                }

                @Override
                public void onFailed(RequestException e) {
                    int requestCode = getRequestCode();
                    if (mListener != null && mListener.onFailed(e, requestCode)) {
                        stopExecute();
                    }
                    checkFinished();
                }

                private void checkFinished() {
                    if (mResults.size() == mRequestPool.size()) {
                        stopExecute();
                        if (mListener != null) {
                            mListener.onSuccess(mResults);
                        }
                    }
                }
            });
        }
        mExecutor.shutdown();
    }

}
