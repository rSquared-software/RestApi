package software.rsquared.restapi;

/**
 * @author Rafal Zajfert
 */
public abstract class MockFactory {

	public <T> T getMockResponse(Request<T> request) {
		return null;
	}
}
