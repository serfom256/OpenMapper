package com.openmapper.exceptions;

import java.sql.SQLException;

public class QueryExecutionError extends SQLException {

    public QueryExecutionError(String reason, String state) {
        super(reason, state);
    }

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
