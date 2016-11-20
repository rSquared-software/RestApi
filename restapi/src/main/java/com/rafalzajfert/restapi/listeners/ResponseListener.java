package com.rafalzajfert.restapi.listeners;

import com.rafalzajfert.restapi.Request;
import com.rafalzajfert.restapi.exceptions.RequestException;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 */
public interface ResponseListener<T> {

    /**
     * All request task ends successfully and returns result
     * @param result result object of request execution
     */
    void onSuccess(T result);

    /**
     * Request execution failed and {@link RuntimeException} was thrown
     * @param e exception with cause of the fail
     */
    void onFailed(RequestException e);
}