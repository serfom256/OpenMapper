package com.openmapper.exceptions;

public class EntityFieldAccessException extends Exception {

    public EntityFieldAccessException() {
        super();
    }

    public EntityFieldAccessException(String message) {
        super(message);
    }

    public EntityFieldAccessException(Throwable cause) {
        super(cause);
    }
}
