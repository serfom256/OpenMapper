package com.openmapper.exceptions.common;

public class FunctionNotFoundException extends RuntimeException {

    public FunctionNotFoundException() {
        super("Function with the specified name not found");
    }

    public FunctionNotFoundException(String message) {
        super(String.format("Function with the specified name: %s not found",  message));
    }
}
