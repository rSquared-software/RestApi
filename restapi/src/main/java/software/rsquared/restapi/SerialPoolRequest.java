package software.rsquared.restapi;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
class SerialPoolRequest extends PoolRequest {
	private Map<Integer, Object> results = new LinkedHashMap<>();
	private Iterator<Map.Entry<Integer, Request>> executeIterator;

	private boolean notified;

	SerialPoolRequest() {
	}

	@Override
	public void execute() {
		executeIterator = requestPool.entrySet().iterator();
		onPreExecute();
		notified = false;
		checkAndExecuteNext(false);
	}

	private void checkAndExecuteNext(boolean forceCancel) {
		if (cancelled.get() || notified) {
			return;
		}

		if (results.size() == requestPool.size()) {
			notified = true;
			onSuccess(results);
			onPostExecute();
		} else if (forceCancel) {
			notified = true;
			cancel();
			onPostExecute();
		} else if (executeIterator.hasNext()) {
			executeNext();
		} else {
			throw new IllegalStateException("results size != requests size and queue is empty!");
		}
	}

	private void executeNext() {
		final Map.Entry<Integer, Request> entry = executeIterator.next();
		Integer requestCode = entry.getKey();
		Request request = entry.getValue();

		//noinspection unchecked
		request.execute(api, new PoolRequestListener(requestCode) {

			@Override
			public void onSuccess(Object result) {
				int requestCode = getRequestCode();
				SerialPoolRequest.this.onTaskSuccess(result, requestCode);
				results.put(requestCode, result);
				checkAndExecuteNext(false);
			}

			@Override
			public void onFailed(RequestException e) {
				int requestCode = getRequestCode();
				SerialPoolRequest.this.onFailed(e, requestCode);
				if (SerialPoolRequest.this.canContinueAfterFailed(e, requestCode)) {
					results.put(requestCode, null);
					checkAndExecuteNext(false);
				} else {
					checkAndExecuteNext(true);
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
