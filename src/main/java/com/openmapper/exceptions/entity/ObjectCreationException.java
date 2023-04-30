package com.openmapper.exceptions.entity;

public class ObjectCreationException extends RuntimeException {

    public ObjectCreationException() {
        super();
    }

    public ObjectCreationException(String message) {
        super(message);
    }

    public ObjectCreationException(Throwable cause) {
        super(cause);
    }
}
