package com.rafalzajfert.restapi.listeners;

import com.rafalzajfert.restapi.Request;

/**
 * Listener for the {@link Request} that allows to receive signal when request execution finished
 *
 * @author Rafał Zajfert
 * @deprecated Use {@link RequestListener} instead
 */
@Deprecated
public abstract class SimplyResponseListener<T> extends RequestListener<T> {

}