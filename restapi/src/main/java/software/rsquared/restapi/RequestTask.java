package software.rsquared.restapi;

import android.support.annotation.WorkerThread;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * @author Rafał Zajfert
 */
public abstract class RequestTask implements Runnable {

	private boolean cancelled;

	RequestTask() {
	}

	@WorkerThread
	@Override
	public final void run() {
		try {
			execute();
		} catch (RequestException e) {
			onFailed(e);
		}
	}

	protected abstract void execute() throws RequestException;

	protected abstract void onFailed(RequestException e);

	public void cancel() {
		cancelled = true;
		try {
			Thread.currentThread().interrupt();
		} catch (Exception ignored) {
		}
	}

	public boolean isCancelled() {
		return cancelled;
	}
}
