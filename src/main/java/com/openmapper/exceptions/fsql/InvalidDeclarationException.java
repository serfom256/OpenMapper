package com.openmapper.exceptions.fsql;

public class InvalidDeclarationException extends RuntimeException {

    public InvalidDeclarationException(final String functionName, final String message) {
        super(String.format("Invalid function: %s declaration. %s", functionName, message));
    }
}
