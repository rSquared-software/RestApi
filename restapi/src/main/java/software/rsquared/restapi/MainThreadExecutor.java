package software.rsquared.restapi;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * @author Rafał Zajfert
 */
class MainThreadExecutor implements Executor {
	private Handler handler = new Handler(Looper.getMainLooper());

	@Override
	public void execute(@NonNull Runnable command) {
		handler.post(command);
	}
}