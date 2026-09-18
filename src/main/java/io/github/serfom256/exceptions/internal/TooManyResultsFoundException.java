package io.github.serfom256.exceptions.internal;

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
