package software.rsquared.restapi;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * @author Rafał Zajfert
 */
public interface Checker {
	void check(Request request) throws RequestException;
}
