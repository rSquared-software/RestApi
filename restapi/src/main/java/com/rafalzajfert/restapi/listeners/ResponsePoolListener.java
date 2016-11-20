package com.rafalzajfert.restapi.listeners;

import android.support.annotation.NonNull;

import com.rafalzajfert.restapi.exceptions.RequestException;

import java.util.Map;

/**
 * TODO dokumentacja
 *
 * @author Rafal Zajfert
 */
public interface ResponsePoolListener {

    /**
     * Called when task successfully finished
     */
    void onTaskSuccess(Object result, int requestCode);

    /**
     * Called when all requests successfully finished
     * @param result results map, if request failed then value will be null
     */
    void onSuccess(@NonNull Map<Integer, Object> result);

	/**
     * Returns true if all unfinished requests should be cancelled, false otherwise
     */
    boolean onFailed(RequestException e, int requestCode);
}