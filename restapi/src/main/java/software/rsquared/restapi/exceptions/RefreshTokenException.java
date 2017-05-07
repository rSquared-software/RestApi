package software.rsquared.restapi.exceptions;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * @author Rafal Zajfert
 */
public class RefreshTokenException extends RuntimeException {

    public RefreshTokenException() {
    }

    public RefreshTokenException(String message) {
        super(message);
    }

    public RefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public RefreshTokenException(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public RefreshTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
