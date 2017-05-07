package software.rsquared.restapi.listeners;

import software.rsquared.restapi.Request;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 */
public abstract class RequestListener<T> {

    /**
     * this method will be invoked before request execution
     */
    public void onPreExecute() {
    }

    /**
     * All request task ends successfully and returns result
     *
     * @param result result object of request execution
     */
    public abstract void onSuccess(T result);

    /**
     * Request execution failed and {@link RuntimeException} was thrown
     *
     * @param e exception with cause of the fail
     */
    public abstract void onFailed(RequestException e);

    /**
     * this method will be invoked after request execution (regardless of the response result).
     */
    public void onPostExecute() {
    }

    public void onCancel(){
    }
}