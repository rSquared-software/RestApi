package software.rsquared.restapi.listeners;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * Callbac that allows to catch all exceptions from returns by the requests
 *
 * @author Rafal Zajfert
 */
public interface ErrorCallback {
    void onError(RequestException e);
}
