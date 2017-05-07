package software.rsquared.restapi;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestPoolListener;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class SerialPoolRequest extends PoolRequest<SerialPoolRequest> {

    private RequestPoolListener listener;
    private Map<Integer, Object> results = new LinkedHashMap<>();
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
        if (!cancelled && executeIterator.hasNext()) {
            final Map.Entry<Integer, Request> requestEntry = executeIterator.next();
            executor.submit(requestEntry.getValue().createRequestTask(), requestEntry.getValue().isErrorCallbackIgnored() ? null : RestApi.getConfiguration().getErrorCallback(), new PoolRequestListener(requestEntry.getKey()) {
                @Override
                public void onSuccess(Object result) {
                    int requestCode = getRequestCode();
                    results.put(requestCode, result);
                    if (listener != null) {
                        listener.onTaskSuccess(result, requestCode);
                    }
                    executeNext();
                }

                @Override
                public void onFailed(RequestException e) {
                    int requestCode = getRequestCode();
                    if (listener == null || !listener.onFailed(e, requestCode)) {
                        results.put(requestCode, null);
                        executeNext();
                    }
                }

                @Override
                public void onCancel() {
                    int requestCode = getRequestCode();
                    cancelled = true;
                    results.put(requestCode, null);
                    executeNext();
                }
            });
        } else {
            stopExecute();
            if (listener != null) {
                if (cancelled) {
                    listener.onCancel();
                } else {
                    if (results.size() == requestPool.size()) {
                        listener.onSuccess(results);
                    }
                    listener.onPostExecute();
                }
            }
        }
    }
}
