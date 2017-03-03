package software.rsquared.restapi;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestPoolListener;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class SerialPoolRequest extends PoolRequest<SerialPoolRequest> {

    private RequestPoolListener mListener;
    private Map<Integer, Object> mResults = new LinkedHashMap<>();
    private Iterator<Map.Entry<Integer, Request>> mExecuteIterator;

    public SerialPoolRequest() {
        super(1);
    }


    public void execute(RequestPoolListener listener) {
        if (mExecuted) {
            throw new IllegalStateException("Already executed.");
        }
        mExecuted = true;
        mExecuteIterator = mRequestPool.entrySet().iterator();
        mListener = listener;

        if (mListener != null) {
            mListener.onPreExecute();
        }
        executeNext();
    }

    private void executeNext() {
        if (mExecuteIterator.hasNext()) {
            Map.Entry<Integer, Request> requestEntry = mExecuteIterator.next();
            mExecutor.submit(requestEntry.getValue().createRequestTask(), new PoolRequestListener(requestEntry.getKey()) {
                @Override
                public void onSuccess(Object result) {
                    int requestCode = getRequestCode();
                    mResults.put(requestCode, result);
                    if (mListener != null) {
                        mListener.onTaskSuccess(result, requestCode);
                    }
                    executeNext();
                }

                @Override
                public void onFailed(RequestException e) {
                    int requestCode = getRequestCode();
                    if (mListener == null || !mListener.onFailed(e, requestCode)) {
                        mResults.put(requestCode, null);
                        executeNext();
                    }
                }
            });
        } else {
            stopExecute();
            if (mListener != null) {
                if (mResults.size() == mRequestPool.size()) {
                    mListener.onSuccess(mResults);
                }
                mListener.onPostExecute();
            }
        }
    }
}
