package com.rafalzajfert.restapi.exceptions;

/**
 * Exception throws when initial requirements failed
 *
 * @author Rafal Zajfert
 */
public class InitialRequirementsException extends Exception {
    public InitialRequirementsException() {
    }

    public InitialRequirementsException(String detailMessage) {
        super(detailMessage);
    }

    public InitialRequirementsException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InitialRequirementsException(Throwable throwable) {
        super(throwable);
    }
}
