package com.openmapper.exceptions.fsql;

import com.openmapper.parser.model.SpanLocation;

public class FsqlParsingException extends IllegalStateException {
    public FsqlParsingException(String fileName, SpanLocation location) {
        super("Unable to parse file: " + fileName + " in: " + location);
    }
}