package com.openmapper.exceptions;

public class ObjectCreationException extends Exception{

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
