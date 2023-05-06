package com.openmapper.exceptions.internal;

public class QueryExecutionError extends RuntimeException {


    public QueryExecutionError(String reason) {
        super(reason);
    }

    public QueryExecutionError() {
        super();
    }

    public QueryExecutionError(Throwable cause) {
        super(cause);
    }
}
