package com.openmapper.exceptions.common;

import java.util.Arrays;

public class UnsupportedTypeException extends RuntimeException {

    public UnsupportedTypeException(String fieldType) {
        super("Unsupported type: " + fieldType);
    }

    public UnsupportedTypeException(String fieldType, Class<?>... allowedTypes) {
        super("Unsupported field type: " +
                fieldType +
                ". Use one of supported types: " +
                Arrays.toString(allowedTypes));
    }
}
