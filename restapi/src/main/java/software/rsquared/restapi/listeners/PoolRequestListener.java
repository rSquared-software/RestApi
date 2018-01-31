package software.rsquared.restapi.listeners;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.Map;

import software.rsquared.restapi.Request;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Listener for the pool of the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafal Zajfert
 */
public interface PoolRequestListener {

	/**
	 * this method will be invoked before all requests execution
	 */
	@MainThread
	default void onPreExecute() {
	}

	/**
	 * Called when task successfully finished
	 */
	@WorkerThread
	default void onTaskSuccess(Object result, int requestCode) {
	}

	/**
	 * Called when all requests successfully (or {@link #canContinueAfterFailed(RequestException, int)} returns true) finished
	 *
	 * @param result results map, if request failed then value will be null
	 */
	@MainThread
	void onSuccess(@NonNull Map<Integer, Object> result);


	/**
	 * Called when task failed<p>
	 * If you want to stop execution after failed, please override {@link #canContinueAfterFailed(RequestException, int)} method.
	 */
	@MainThread
	void onFailed(RequestException e, int requestCode);

	/**
	 * Returns false if all unfinished requests should be cancelled, true otherwise
	 */
	@WorkerThread
	default boolean canContinueAfterFailed(RequestException e, int requestCode) {
		return true;
	}

	/**
	 * this method will be invoked after all request executions (regardless of the response result).
	 */
	@MainThread
	default void onPostExecute() {
	}

	@MainThread
	default void onCanceled() {
	}
}
