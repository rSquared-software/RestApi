package software.rsquared;

import software.rsquared.restapi.PostRequest;
import software.rsquared.restapi.RestApi;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.sample.MainActivity;

/**
 * @author Rafał Zajfert
 */
public class AnotherAppClass {
	public static void getVersion() {

		RestApi.execute(new GetVersion(), new RequestListener<MainActivity.Version>() {
			@Override
			public void onSuccess(MainActivity.Version result) {

//				Logger.error("onSuccess");
//				Logger.debug(result);
			}

			@Override
			public void onFailed(RequestException e) {
//				Logger.error(e);
			}
		});
	}
	public static class GetVersion extends PostRequest<MainActivity.Version> {

		@Override
		protected void prepareRequest() {
			setUrlSegments("api", "version");
			putUrlParameter("param1", "value1");
			putParameter("param2", "value2");
		}
	}
}
