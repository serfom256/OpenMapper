package io.github.serfom256.exceptions.internal;

public class ResultSetExtractionException extends RuntimeException{
    public ResultSetExtractionException() {
        super();
    }

    public ResultSetExtractionException(String message) {
        super(message);
    }

    public ResultSetExtractionException(Throwable cause) {
        super(cause);
    }
}
