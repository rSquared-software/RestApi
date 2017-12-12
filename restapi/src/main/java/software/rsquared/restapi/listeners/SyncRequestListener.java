package software.rsquared.restapi.listeners;

import software.rsquared.restapi.Request;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 */
public abstract class SyncRequestListener<T> extends RequestListener<T> {

	@Override
	public void onSuccess(T result) {

	}
}