package com.openmapper.exceptions.fsql;

public class InvalidFileFormatException extends IllegalStateException {
    public InvalidFileFormatException(String fileName) {
        super(String.format("Illegal file extension: %s. File extension must be [.fsql]", fileName));
    }
}
