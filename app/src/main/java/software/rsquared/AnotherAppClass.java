package software.rsquared;

import android.support.annotation.NonNull;

import java.util.Map;

import software.rsquared.androidlogger.Logger;
import software.rsquared.restapi.PoolRequest;
import software.rsquared.restapi.PostRequest;
import software.rsquared.restapi.RestApi;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.PoolRequestListener;
import software.rsquared.restapi.sample.MainActivity;

/**
 * @author Rafał Zajfert
 */
public class AnotherAppClass {
	public static void getVersion(RestApi api) {


//		api.execute(new GetVersion(), new RequestListener<MainActivity.Version>() {
//			@Override
//			public void onSuccess(MainActivity.Version result) {
//
////				Logger.error("onSuccess");
////				Logger.debug(result);
//			}
//
//			@Override
//			public void onFailed(RequestException e) {
////				Logger.error(e);
//			}
//		});

		api.execute(PoolRequest.create(PoolRequest.THREAD_POOL_EXECUTOR)
						.addTask(new GetVersion(), 1)
						.addTask(new GetVersion(), 2),
				new PoolRequestListener() {
					@Override
					public void onSuccess(@NonNull Map<Integer, Object> result) {
						Logger.debug("onSuccess() called with: result = [" + result + "]");
						for (Map.Entry<Integer, Object> entry : result.entrySet()) {
							Logger.error(entry.getKey(), "=", entry.getValue());
						}
					}

					@Override
					public void onFailed(RequestException e, int requestCode) {
						Logger.debug("onFailed() called with: e = [" + e + "], requestCode = [" + requestCode + "]");
					}

					@Override
					public boolean canContinueAfterFailed(RequestException e, int requestCode) {
						return true;
					}
				});
	}

	public static class GetVersion extends PostRequest<MainActivity.Version> {

		@Override
		protected void prepareRequest() {
			addPathSegments("api", "version");
			putQueryParameter("param1", "value1");
			putParameter("param2", "value2");
//			putParameter("asd");
		}
	}
}
