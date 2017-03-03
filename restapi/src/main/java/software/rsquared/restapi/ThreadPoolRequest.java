package software.rsquared.restapi;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestPoolListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class ThreadPoolRequest extends PoolRequest<ThreadPoolRequest> {

    private Map<Integer, Object> mResults = new LinkedHashMap<>();
    private RequestPoolListener mListener;

    public ThreadPoolRequest(int poolSize) {
        super(poolSize);
    }

    public void execute(RequestPoolListener listener) {
        if (mExecuted) {
            throw new IllegalStateException("Already executed.");
        }
        mExecuted = true;

        mListener = listener;
        if (mListener != null) {
            mListener.onPreExecute();
        }
        for (Map.Entry<Integer, Request> entry : mRequestPool.entrySet()) {
            mExecutor.submit(entry.getValue().createRequestTask(), new PoolRequestListener(entry.getKey()) {
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
                    } else {
                        mResults.put(requestCode, null);
                    }
                    checkFinished();
                }

                private void checkFinished() {
                    if (mResults.size() == mRequestPool.size()) {
                        stopExecute();
                        if (mListener != null) {
                            mListener.onSuccess(mResults);
                            mListener.onPreExecute();
                        }
                    }
                }
            });
        }
        mExecutor.shutdown();
    }

}
