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

    private Map<Integer, Object> results = new LinkedHashMap<>();
    private RequestPoolListener listener;

    public ThreadPoolRequest(int poolSize) {
        super(poolSize);
    }

    public void execute(RequestPoolListener listener) {
        if (executed) {
            throw new IllegalStateException("Already executed.");
        }
        executed = true;

        this.listener = listener;
        if (this.listener != null) {
            this.listener.onPreExecute();
        }
        for (Map.Entry<Integer, Request> entry : requestPool.entrySet()) {
            executor.submit(entry.getValue().createRequestTask(), entry.getValue().isErrorCallbackIgnored() ? null : RestApi.getConfiguration().getErrorCallback(), new PoolRequestListener(entry.getKey()) {
                @Override
                public void onSuccess(Object result) {
                    int requestCode = getRequestCode();
                    results.put(requestCode, result);
                    if (ThreadPoolRequest.this.listener != null) {
                        ThreadPoolRequest.this.listener.onTaskSuccess(result, requestCode);
                    }
                    checkFinished();
                }

                @Override
                public void onFailed(RequestException e) {
                    int requestCode = getRequestCode();
                    if (ThreadPoolRequest.this.listener != null && ThreadPoolRequest.this.listener.onFailed(e, requestCode)) {
                        stopExecute();
                    } else {
                        results.put(requestCode, null);
                    }
                    checkFinished();
                }

                @Override
                public void onCancel() {
                    cancelled = true;
                }

                private void checkFinished() {
                    if (results.size() == requestPool.size() || cancelled) {
                        stopExecute();
                        if (ThreadPoolRequest.this.listener != null) {
                            if (cancelled){
                                ThreadPoolRequest.this.listener.onCancel();
                            }else {
                                ThreadPoolRequest.this.listener.onSuccess(results);
                            }
                            ThreadPoolRequest.this.listener.onPostExecute();
                        }
                    }
                }
            });
        }
        executor.shutdown();
    }

}
