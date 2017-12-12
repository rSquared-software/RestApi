package software.rsquared.restapi;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface Authorization {

	/**
	 * Returns current access token for requests authorization
	 */
	String getAccessToken();
}
