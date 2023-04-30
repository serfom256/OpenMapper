package com.openmapper.core.query.impl;

public class TooManyResultsFoundException extends RuntimeException{
    public TooManyResultsFoundException() {
        super();
    }

    public TooManyResultsFoundException(String message) {
        super(message);
    }

    public TooManyResultsFoundException(Throwable cause) {
        super(cause);
    }
}
