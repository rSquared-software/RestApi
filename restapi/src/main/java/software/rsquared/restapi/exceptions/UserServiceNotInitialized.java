package software.rsquared.restapi.exceptions;

/**
 * @author Rafal Zajfert
 */
public class UserServiceNotInitialized extends AccessTokenException {

    public UserServiceNotInitialized() {
    }

    public UserServiceNotInitialized(String message) {
        super(message);
    }

    public UserServiceNotInitialized(String message, Throwable cause) {
        super(message, cause);
    }

    public UserServiceNotInitialized(Throwable cause) {
        super(cause);
    }

    public UserServiceNotInitialized(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
