package com.rafalzajfert.restapi.exceptions;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Exception with errors from Api
 *
 * @author Rafal Zajfert
 */
@SuppressWarnings("unused")
public class RequestException extends ExecutionException {

    /**
     * Request completed
     */
    public static final int CODE_OK = 200;

    /**
     * Request accepted but data is incomplete / invalid
     */
    public static final int CODE_ACCEPTED = 202;

    /**
     * The request was invalid. You may be missing a required argument or provided bad data. An error mName will be returned explaining what happened.
     */
    public static final int CODE_BAD_REQUEST = 400;

    /**
     * The authentication you provided is invalid.
     */
    public static final int CODE_UNAUTHORIZED = 401;

    /**
     * You don't have permission to complete the operation or access the resource.
     */
    public static final int CODE_FORBIDDEN = 403;

    /**
     * You requested an invalid method.
     */
    public static final int CODE_NOT_FOUND = 404;

    /**
     * The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. (used POST instead of PUT)
     */
    public static final int CODE_METHOD_NOT_ALLOWED = 405;

    /**
     * The server timed out waiting for the request.
     */
    public static final int CODE_TIMEOUT = 408;

    /**
     * You have exceeded the rate limit.
     */
    public static final int CODE_TOO_MANY_REQUESTS = 429;

    /**
     * Something is wrong on our end. We'll investigate what happened.
     */
    public static final int CODE_INTERNAL_SERVER_ERROR = 500;

    /**
     * The method you requested is currently unavailable (due to maintenance or high load).
     */
    public static final int CODE_SERVICE_UNAVAILABLE = 503;

    /**
     * unknown, non server exception
     */
    public static final int UNKNOWN = -1;

    /**
     * Waiting thread is activated before the condition it was waiting for has been satisfied.
     */
    public static final int INTERRUPTED = -2;

    /**
     * Blocking operation times out
     */
    public static final int TIMEOUT = -3;

    /**
     * Obtaining access token before request execution failed
     */
    public static final int INVALID_ACCESS_TOKEN = -3;

    private final int mResponseCode;

    private final String mName;

    private final String mMessage;

    private final int mErrorCode;

    private final Map<String, String[]> mErrorsMap;

    public RequestException(@NonNull Exception e) {
        super(e);
        mName = "Internal error";
        mMessage = e.getMessage();
        if (e instanceof InterruptedException) {
            mResponseCode = RequestException.INTERRUPTED;
        } else if (e instanceof TimeoutException) {
            mResponseCode = RequestException.TIMEOUT;
        } else if (e instanceof AccessTokenException) {
            mResponseCode = RequestException.INVALID_ACCESS_TOKEN;
        } else {
            mResponseCode = RequestException.UNKNOWN;
        }
        mErrorCode = UNKNOWN;
        mErrorsMap = new HashMap<>();
    }

    public RequestException(int responseCode, String name, String message, int errorCode, Map<String, String[]> errorsMap) {
        mResponseCode = responseCode;
        mName = name;
        mMessage = message;
        mErrorCode = errorCode;
        mErrorsMap = errorsMap;
    }

    /**
     * Http response code
     * @see #getErrorCode()
     */
    public int getResponseCode() {
        return mResponseCode;
    }

    /**
     * The name of the error
     */
    public String getName() {
        return mName;
    }

    /**
     * Message of the error
     */
    @Override
    public String getMessage() {
        return mMessage;
    }

    /**
     * Custom error code.
     * @see #getResponseCode()
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * Map with fields errors
     */
    public Map<String, String[]> getErrorsMap() {
        return mErrorsMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(mErrorCode).append(" (").append(mResponseCode).append(") ");
        if (!TextUtils.isEmpty(mName)) {
            builder.append(mName);
        }
        if (!TextUtils.isEmpty(mMessage)) {
            builder.append("\n");
            builder.append(mMessage);
        }
        for (Map.Entry<String, String[]> entry : mErrorsMap.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey()).append(": ").append(Arrays.toString(entry.getValue()));
        }
        return builder.toString();
    }
}
