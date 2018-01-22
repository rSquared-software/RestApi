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
class SerialPoolRequest extends PoolRequest<SerialPoolRequest> {
	private Map<Integer, Object> results = new LinkedHashMap<>();
	private Iterator<Map.Entry<Integer, Request>> executeIterator;

	public SerialPoolRequest() {
		super(1);
	}

	@Override
	public void execute() {
		if (executed) {
			throw new IllegalStateException("Already executed.");
		}
		executed = true;
		executeIterator = requestPool.entrySet().iterator();
		onPreExecute();
		executeNext();
	}

	private void executeNext() {
		if (!cancelled && executeIterator.hasNext()) {
			final Map.Entry<Integer, Request> requestEntry = executeIterator.next();
			//noinspection unchecked
			executor.submit(requestEntry.getValue().createTask(api, listener), requestEntry.getValue().isErrorCallbackIgnored() ? null : RestApi.getConfiguration().getErrorCallback(), new PoolRequestListener(requestEntry.getKey()) {
				@Override
				public void onSuccess(Object result) {
					int requestCode = getRequestCode();
					SerialPoolRequest.this.onTaskSuccess(result, requestCode);
					results.put(requestCode, result);
					executeNext();
				}

				@Override
				public void onFailed(RequestException e) {
					int requestCode = getRequestCode();
					SerialPoolRequest.this.onFailed(e, requestCode);
					if (canContinueAfterFailed(e, requestCode)) {
						results.put(requestCode, null);
						executeNext();
					}
				}

				@Override
				public void onCanceled() {
					int requestCode = getRequestCode();
					cancelled = true;
					results.put(requestCode, null);
					executeNext();
				}
			});
		} else {
			stopExecute();
			if (cancelled) {
				SerialPoolRequest.this.onCanceled();
			} else {
				if (results.size() == requestPool.size()) {
					SerialPoolRequest.this.onSuccess(results);
				}
			}
			SerialPoolRequest.this.onPostExecute();
		}
	}
}
