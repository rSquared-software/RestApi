package software.rsquared.restapi.listeners;

import software.rsquared.restapi.Request;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 */
public interface SyncRequestListener<T> extends RequestListener<T> {

	@Override
	default void onSuccess(T result) {
	}
}