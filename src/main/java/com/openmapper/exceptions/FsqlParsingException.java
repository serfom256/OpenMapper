package com.openmapper.exceptions;

public class FsqlParsingException extends IllegalStateException {

    public FsqlParsingException(String fileName, Integer line, Integer position) {
        super("Unable to parse file: " + fileName + " in line: " + line + " at: " + position);
    }

    public FsqlParsingException(String fileName, Integer line) {
        super("Unable to parse file: " + fileName + " in line: " + line);
    }

    public FsqlParsingException(String fileName) {
        super("Unable to parse file: " + fileName);
    }
}
