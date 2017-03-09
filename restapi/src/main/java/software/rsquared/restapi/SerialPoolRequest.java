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

    private RequestPoolListener listener;
    private Map<Integer, Object> mResults = new LinkedHashMap<>();
    private Iterator<Map.Entry<Integer, Request>> executeIterator;

    public SerialPoolRequest() {
        super(1);
    }


    public void execute(RequestPoolListener listener) {
        if (executed) {
            throw new IllegalStateException("Already executed.");
        }
        executed = true;
        executeIterator = requestPool.entrySet().iterator();
        this.listener = listener;

        if (this.listener != null) {
            this.listener.onPreExecute();
        }
        executeNext();
    }

    private void executeNext() {
        if (executeIterator.hasNext()) {
            Map.Entry<Integer, Request> requestEntry = executeIterator.next();
            executor.submit(requestEntry.getValue().createRequestTask(), ignoreErrorCallback ? null : RestApi.getConfiguration().getErrorCallback(), new PoolRequestListener(requestEntry.getKey()) {
                @Override
                public void onSuccess(Object result) {
                    int requestCode = getRequestCode();
                    mResults.put(requestCode, result);
                    if (listener != null) {
                        listener.onTaskSuccess(result, requestCode);
                    }
                    executeNext();
                }

                @Override
                public void onFailed(RequestException e) {
                    int requestCode = getRequestCode();
                    if (listener == null || !listener.onFailed(e, requestCode)) {
                        mResults.put(requestCode, null);
                        executeNext();
                    }
                }
            });
        } else {
            stopExecute();
            if (listener != null) {
                if (mResults.size() == requestPool.size()) {
                    listener.onSuccess(mResults);
                }
                listener.onPostExecute();
            }
        }
    }
}
