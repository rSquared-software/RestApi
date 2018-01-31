package software.rsquared.restapi;

import android.support.annotation.WorkerThread;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;

/**
 * @author Rafał Zajfert
 */
public abstract class RequestTask<T> implements Runnable {
	protected final RequestListener<T> listener;

	RequestTask(RequestListener<T> listener) {
		this.listener = listener;
	}

	@WorkerThread
	@Override
	public final void run() {
		onPreExecute();
		try {
			onSuccess(execute());
		} catch (RequestException e) {
			onFailed(e);
		}
		onPostExecute();
	}

	@WorkerThread
	protected void onPreExecute() {
	}

	@WorkerThread
	protected abstract T execute() throws RequestException;

	@WorkerThread
	protected abstract void onSuccess(T result);

	@WorkerThread
	protected abstract void onFailed(RequestException e);

	@WorkerThread
	protected void onPostExecute() {

	}

	@WorkerThread
	protected void onCancelled() {

	}
}
