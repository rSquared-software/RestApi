package software.rsquared.restapi.listeners;

import android.support.annotation.UiThread;

import software.rsquared.restapi.Request;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 */
public interface RequestListener<T> {

	/**
	 * this method will be invoked before request execution
	 */
	@UiThread
	default void onPreExecute() {
	}

	/**
	 * All request task ends successfully and returns result
	 *
	 * @param result result object of request execution
	 */
	@UiThread
	void onSuccess(T result);

	/**
	 * Request execution failed and {@link RuntimeException} was thrown
	 *
	 * @param e exception with cause of the fail
	 */
	@UiThread
	default void onFailed(RequestException e){

	}

	/**
	 * this method will be invoked after request execution (regardless of the response result).
	 */
	@UiThread
	default void onPostExecute() {
	}

	@UiThread
	default void onCanceled() {
	}
}