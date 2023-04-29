package com.openmapper.exceptions.internal;

public class InvalidResultCountException extends RuntimeException{
    public InvalidResultCountException() {
    }

    public InvalidResultCountException(String message) {
        super(message);
    }

    public InvalidResultCountException(Throwable cause) {
        super(cause);
    }
}
