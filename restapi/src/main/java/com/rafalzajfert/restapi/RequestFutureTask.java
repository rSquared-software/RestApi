package com.rafalzajfert.restapi;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rafalzajfert.restapi.exceptions.RequestException;
import com.rafalzajfert.restapi.listeners.ResponseListener;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A cancellable asynchronous computation. This implementation converts all Exception to the {@link RequestException} and allows listen to the end of execution
 *
 * @author Rafał Zajfert
 * @see RequestFuture
 * @see FutureTask
 */
class RequestFutureTask<T> extends FutureTask<T> implements RequestFuture<T> {

    @Nullable
    private ResponseListener<T> mListener;

    @Nullable
    private static Handler mHandler;

    /**
     * Creates a FutureTask that will, upon running, execute the given Callable.
     *
     * @param callable the callable task
     */
    RequestFutureTask(@NonNull Callable<T> callable) {
        super(callable);
    }

    /**
     * Creates a FutureTask that will, upon running, execute the given Callable.
     *
     * @param callable the callable task
     * @param listener the listener that will be called when execution finished
     */
    RequestFutureTask(@NonNull Callable<T> callable, @Nullable ResponseListener<T> listener) {
        super(callable);
        mListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() throws RequestException {
        try {
            return super.get();
        } catch (ExecutionException | InterruptedException e) {
            throw parseException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(long timeout, @NonNull TimeUnit unit) throws RequestException {
        try {
            return super.get(timeout, unit);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw parseException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void done() {
        if (mListener != null) {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mListener.onSuccess(get());
                    } catch (RequestException e) {
                        mListener.onFailed(e);
                    }
                }
            });
        }
        super.done();
    }

    /**
     * Convert all type of the exception to {@link RuntimeException} instance.<br />
     * <b>Note:</b> {@link RuntimeException} will be thrown immediately.
     *
     * @param e instance of exception that should be wrapped
     */
    @NonNull
    private RequestException parseException(Exception e) {
        Throwable cause = e.getCause();
        if (cause != null) {
            if (cause instanceof RequestException) {
                return (RequestException) cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
        }
        return new RequestException(e);
    }

    /**
     * Get handler for the main looper
     */
    @NonNull
    private Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }

}
