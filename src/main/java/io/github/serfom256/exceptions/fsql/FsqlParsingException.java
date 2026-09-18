package io.github.serfom256.exceptions.fsql;

import io.github.serfom256.parser.model.SpanLocation;

public class FsqlParsingException extends IllegalStateException {
    public FsqlParsingException(String fileName, SpanLocation location) {
        super("Unable to parse file: " + fileName + " in: " + location);
    }
}