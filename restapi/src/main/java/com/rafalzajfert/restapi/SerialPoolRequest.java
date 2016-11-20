package com.rafalzajfert.restapi;

import com.rafalzajfert.restapi.exceptions.RequestException;
import com.rafalzajfert.restapi.listeners.ResponsePoolListener;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
public class SerialPoolRequest extends PoolRequest<SerialPoolRequest> {

    private ResponsePoolListener mListener;
    private Map<Integer, Object> mResults = new LinkedHashMap<>();
    private Iterator<Map.Entry<Integer, Request>> mExecuteIterator;

    public SerialPoolRequest() {
        super(1);
    }


    public void execute(ResponsePoolListener listener) {
        if (mExecuted) {
            throw new IllegalStateException("Already executed.");
        }
        mExecuted = true;
        mExecuteIterator = mRequestPool.entrySet().iterator();
        mListener = listener;

        executeNext();
    }

    private void executeNext() {
        if (mExecuteIterator.hasNext()) {
            Map.Entry<Integer, Request> requestEntry = mExecuteIterator.next();
            mExecutor.submit(requestEntry.getValue().createRequestTask(), new PoolResponseListener(requestEntry.getKey()) {
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
                    if (!mListener.onFailed(e, requestCode)) {
                        executeNext();
                    }
                }
            });
        } else {
            stopExecute();
            if (mListener != null && mResults.size() == mRequestPool.size()) {
                mListener.onSuccess(mResults);
            }
        }
    }
}
