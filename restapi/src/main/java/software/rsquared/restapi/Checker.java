package software.rsquared.restapi;

import android.support.annotation.WorkerThread;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * Created by rafalo on 18.01.2018.
 */
public interface Checker {

	@WorkerThread
	void check(Request request) throws RequestException;

}
