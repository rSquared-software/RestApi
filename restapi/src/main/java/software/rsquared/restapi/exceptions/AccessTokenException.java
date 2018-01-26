package software.rsquared.restapi.exceptions;

/**
 * @author Rafal Zajfert
 */
public class AccessTokenException extends RequestException {

	public AccessTokenException() {
		super("AuthorizationException", "Invalid access token");
	}
}
