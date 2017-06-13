package software.rsquared.restapi.exceptions;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.net.SocketTimeoutException;
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
     * The request was invalid. You may be missing a required argument or provided bad data. An error name will be returned explaining what happened.
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

    private final int responseCode;

    private final String name;

    private final String message;

    private final int errorCode;

    private final Map<String, String[]> errorsMap;

    public RequestException(@NonNull Exception e) {
        super(e);
        name = "Internal error";
        message = e.getMessage();
        if (e instanceof InterruptedException) {
            responseCode = RequestException.INTERRUPTED;
        } else if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
            responseCode = RequestException.CODE_TIMEOUT;
        } else if (e instanceof AccessTokenException) {
            responseCode = RequestException.CODE_UNAUTHORIZED;
        } else {
            responseCode = RequestException.UNKNOWN;
        }
        errorCode = UNKNOWN;
        errorsMap = new HashMap<>();
    }

    public RequestException(int responseCode, String name, String message, int errorCode, Map<String, String[]> errorsMap) {
        this.responseCode = responseCode;
        this.name = name;
        this.message = message;
        this.errorCode = errorCode;
        this.errorsMap = errorsMap;
    }

    /**
     * Http response code
     *
     * @see #getErrorCode()
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * The name of the error
     */
    public String getName() {
        return name;
    }

    /**
     * Message of the error
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Custom error code.
     *
     * @see #getResponseCode()
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Map with fields errors
     */
    public Map<String, String[]> getErrorsMap() {
        return errorsMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getName());
        builder.append(" ").append(errorCode).append("[").append(responseCode).append("] ");
        if (!TextUtils.isEmpty(name)) {
            builder.append(name);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.append("\n");
            builder.append(message);
        }
        for (Map.Entry<String, String[]> entry : errorsMap.entrySet()) {
            builder.append("\n");
            builder.append(entry.getKey()).append(": ").append(Arrays.toString(entry.getValue()));
        }
        return builder.toString() + "\n" + super.toString();
    }
}
