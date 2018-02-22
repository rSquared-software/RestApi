package software.rsquared.restapi.listeners;

import android.support.annotation.WorkerThread;

import software.rsquared.restapi.Request;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Callback that allows to catch all exceptions from returns by the requests
 *
 * @author Rafal Zajfert
 */
public interface ErrorCallback {

	/**
	 * @param request that caused error
	 * @param e error
	 * @return true if error was consumed and should not be processed by request listener
	 */
	@WorkerThread
	boolean onError(Request request, RequestException e);

}
