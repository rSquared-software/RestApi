package software.rsquared.restapi.exceptions;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * @author Rafal Zajfert
 */
public class AccessTokenException extends RuntimeException {

    public AccessTokenException() {
    }

    public AccessTokenException(String message) {
        super(message);
    }

    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessTokenException(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public AccessTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
