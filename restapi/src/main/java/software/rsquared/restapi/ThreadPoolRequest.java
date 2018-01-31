package software.rsquared.restapi;

import android.support.annotation.AnyThread;

import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class ThreadPoolRequest extends PoolRequest {

	private Map<Integer, Object> results = new LinkedHashMap<>();

	ThreadPoolRequest() {
	}

	@AnyThread
	public void execute() {
		ThreadPoolRequest.this.onPreExecute();

		for (Map.Entry<Integer, Request> entry : requestPool.entrySet()) {
			Integer requestCode = entry.getKey();
			Request request = entry.getValue();
			ThreadPoolRequest.this.onPreExecute();

			//noinspection unchecked
			request.execute(api, new PoolRequestListener(requestCode) {

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
					if (!cancelled.get()) {
						cancel();
					}
				}

			});
		}
	}

	private void checkFinished(boolean forceCancel) {
		if (!cancelled.get()) {
			boolean allFinished = results.size() == requestPool.size();
			if (allFinished) {
				onSuccess(results);
				onPostExecute();
			} else if (forceCancel) {
				cancel();
				onPostExecute();
			}
		}
	}

}
