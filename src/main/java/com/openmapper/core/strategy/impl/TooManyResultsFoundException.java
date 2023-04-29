package com.openmapper.core.strategy.impl;

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
