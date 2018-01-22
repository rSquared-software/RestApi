package software.rsquared.restapi;

import android.support.annotation.WorkerThread;

import software.rsquared.restapi.exceptions.AccessTokenException;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Created by rafalo on 18.01.2018.
 */

public abstract class AuthChecker implements Checker {

	@Override
	@WorkerThread
	public void check(Request request) throws RequestException {
		if (request instanceof Authorizable) {
			if (!authorizationIsValid()) {
				refreshAuthToken();
			}
			if (!authorizationIsValid()) {
				throw new AccessTokenException();
			}
		}
	}

	/**
	 * @return true when
	 */
	protected abstract boolean authorizationIsValid();

	/**
	 * <p>synchronously refresh access token</p>
	 *
	 * @return
	 */
	@WorkerThread
	protected abstract void refreshAuthToken() ;

}
