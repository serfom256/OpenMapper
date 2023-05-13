package com.openmapper.exceptions.fsql;

public class FsqlParsingException extends IllegalStateException {

    public FsqlParsingException(String fileName, Integer line) {
        super("Unable to parse file: " + fileName + " in line: " + line);
    }
}
