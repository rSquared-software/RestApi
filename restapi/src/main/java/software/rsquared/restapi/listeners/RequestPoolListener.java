package software.rsquared.restapi.listeners;

import android.support.annotation.NonNull;

import java.util.Map;

import software.rsquared.restapi.Request;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Listener for the pool of the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafal Zajfert
 */
public abstract class RequestPoolListener {

    /**
     * this method will be invoked before all requests execution
     */
    public void onPreExecute() {
    }

    /**
     * Called when task successfully finished
     */
    public void onTaskSuccess(Object result, int requestCode) {
    }

    /**
     * Called when all requests successfully (or onFailed returns false) finished
     *
     * @param result results map, if request failed then value will be null
     */
    public abstract void onSuccess(@NonNull Map<Integer, Object> result);

    /**
     * Returns true if all unfinished requests should be cancelled, false otherwise
     */
    public abstract boolean onFailed(RequestException e, int requestCode);

    /**
     * this method will be invoked after all request executions (regardless of the response result).
     */
    public void onPostExecute() {
    }

    public void onCancel() {
    }
}
