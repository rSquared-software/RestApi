package software.rsquared.restapi.exceptions;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * @author Rafal Zajfert
 */
public class RefreshTokenException extends IllegalStateException {

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


}
