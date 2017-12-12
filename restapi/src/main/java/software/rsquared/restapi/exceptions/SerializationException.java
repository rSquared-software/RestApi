package software.rsquared.restapi.exceptions;

import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * @author Rafal Zajfert
 */
public class SerializationException extends RuntimeException {
	public SerializationException() {
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public SerializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
