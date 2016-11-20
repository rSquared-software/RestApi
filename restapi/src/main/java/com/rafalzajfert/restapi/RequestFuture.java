package com.rafalzajfert.restapi;

import android.support.annotation.NonNull;

import com.rafalzajfert.restapi.exceptions.RequestException;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A Future represents the result of an asynchronous computation. This interface wrap default Exception to {@link}
 *
 * @author Rafał Zajfert
 * @see Future
 */
interface RequestFuture<V> extends Future<V> {

    /**
     * {@inheritDoc}
     */
    @Override
    V get() throws RequestException;

    /**
     * {@inheritDoc}
     */
    @Override
    V get(long timeout, @NonNull TimeUnit unit) throws RequestException;

}
