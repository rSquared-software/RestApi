package com.rafalzajfert.restapi.listeners;

import android.support.annotation.NonNull;

import com.rafalzajfert.restapi.exceptions.RequestException;

import java.util.Map;

/**
 * TODO dokumentacja
 *
 * @author Rafal Zajfert
 */
public abstract class SimplyResponsePoolListener implements ResponsePoolListener {

    /**
     * {@inheritDoc}
     */
    public void onTaskSuccess(Object result, int requestCode){}

    /**
     * {@inheritDoc}
     */
    public void onSuccess(@NonNull Map<Integer, Object> result){}

    /**
     * {@inheritDoc}
     */
    public boolean onFailed(RequestException e, int requestCode){ return false; }
}