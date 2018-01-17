package software.rsquared.restapi;

import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class ThreadPoolRequest extends PoolRequest<ThreadPoolRequest> {

	private Map<Integer, Object> results = new LinkedHashMap<>();

	public ThreadPoolRequest(int poolSize) {
		super(poolSize);
	}

	public void execute() {
		if (executed) {
			throw new IllegalStateException("Already executed.");
		}
		executed = true;

		ThreadPoolRequest.this.onPreExecute();
		for (Map.Entry<Integer, Request> entry : requestPool.entrySet()) {
			//noinspection unchecked
			executor.submit(entry.getValue().createRequestTask(), entry.getValue().isErrorCallbackIgnored() ? null : RestApi.getConfiguration().getErrorCallback(), new PoolRequestListener(entry.getKey()) {
				@Override
				public void onSuccess(Object result) {
					int requestCode = getRequestCode();
					ThreadPoolRequest.this.onTaskSuccess(result, requestCode);
					results.put(requestCode, result);
					checkFinished(false);
				}

				@Override
				public void onFailed(RequestException e) {
					int requestCode = getRequestCode();
					ThreadPoolRequest.this.onFailed(e, requestCode);
					if (ThreadPoolRequest.this.canContinueAfterFailed(e, requestCode)) {
						results.put(requestCode, null);
						checkFinished(false);
					} else {
						checkFinished(true);
					}
				}

				@Override
				public void onCanceled() {
					cancelled = true;
				}

				private void checkFinished(boolean forceFinish) {
					if (cancelled) {
						stopExecute();
						ThreadPoolRequest.this.onCanceled();
					} else {
						boolean allFinished = results.size() == requestPool.size();
						if (allFinished || forceFinish) {
							stopExecute();
							if (allFinished) {
								ThreadPoolRequest.this.onSuccess(results);
							}
							ThreadPoolRequest.this.onPostExecute();
						}

					}
				}
			});
		}
		executor.shutdown();
	}

}
