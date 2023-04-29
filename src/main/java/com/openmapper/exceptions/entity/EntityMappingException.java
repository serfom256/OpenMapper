package com.openmapper.exceptions.entity;

public class EntityMappingException extends RuntimeException{

    public EntityMappingException() {
        super();
    }

    public EntityMappingException(String message) {
        super(message);
    }

    public EntityMappingException(Throwable cause) {
        super(cause);
    }
}
