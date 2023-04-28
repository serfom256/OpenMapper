package com.openmapper.exceptions;

public class ObjectInstantiationException extends RuntimeException{

    public ObjectInstantiationException() {
        super();
    }

    public ObjectInstantiationException(String message) {
        super(message);
    }

    public ObjectInstantiationException(Throwable cause) {
        super(cause);
    }
}
