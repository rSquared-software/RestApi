package software.rsquared.restapi;

/**
 * @author Rafal Zajfert
 */
public interface MockFactory {

	default <T> T getMockResponse(Request<T> request) {
		return null;
	}
}
