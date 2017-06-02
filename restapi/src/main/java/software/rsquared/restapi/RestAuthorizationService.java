package software.rsquared.restapi;

import android.support.annotation.WorkerThread;

import software.rsquared.restapi.exceptions.RefreshTokenException;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface RestAuthorizationService {
    boolean isLogged();

    boolean isTokenValid();

    void refreshToken();

    Authorization getAuthorization();

    /**
     * Method called when method {@link #isLogged()} returns false (before request execution).
     * This method is called from background thread so you can try to log in (this method is called in background task) and return true if success
     * @param request executed request
     * @return false if request should stop working, true otherwise
     */
    @WorkerThread
    boolean onNotLogged(Request request);

    /**
     *
     * @return false if request should stop working, true otherwise
     */
    @WorkerThread
    boolean onRefreshTokenFailed(RefreshTokenException e);
}
