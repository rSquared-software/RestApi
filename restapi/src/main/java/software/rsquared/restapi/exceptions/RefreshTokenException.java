package software.rsquared.restapi.exceptions;

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
