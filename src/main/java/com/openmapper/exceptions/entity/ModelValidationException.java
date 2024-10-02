package com.openmapper.exceptions.entity;

public class ModelValidationException extends RuntimeException {

    public ModelValidationException(Class<?> modelClass, String message) {
        super("Cannot validate model: %s, caused by: %s".formatted(modelClass.getSimpleName(), message));
    }
}
