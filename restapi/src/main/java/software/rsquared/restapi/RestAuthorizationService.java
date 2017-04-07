package software.rsquared.restapi;

import android.support.annotation.WorkerThread;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface RestAuthorizationService {
    boolean isLogged();

    boolean isTokenValid();

    void refreshToken();

    void logout();

    Authorization getAuthorization();

    /**
     * Method called when method {@link #isLogged()} returns false (before request execution).
     * This method is called from background thread so you can try to log in (synchronously) and return true if success
     * @param request executed request
     * @return false if request should stop working, true otherwise
     */
    @WorkerThread
    boolean onNotLogged(Request request);
}
