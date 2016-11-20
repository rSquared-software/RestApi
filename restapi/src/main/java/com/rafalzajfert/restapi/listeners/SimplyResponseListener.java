package com.rafalzajfert.restapi.listeners;

import com.rafalzajfert.restapi.Request;
import com.rafalzajfert.restapi.exceptions.RequestException;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 */
public abstract class SimplyResponseListener<T> implements ResponseListener<T> {

    /**
     * {@inheritDoc}
     */
    public void onSuccess(T result){}

    /**
     * {@inheritDoc}
     */
    public void onFailed(RequestException e){}
}