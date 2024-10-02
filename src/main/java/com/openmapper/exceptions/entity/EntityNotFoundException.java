package com.openmapper.exceptions.entity;

import java.sql.SQLException;

public class EntityNotFoundException extends SQLException {

    public EntityNotFoundException(String reason) {
        super(reason);
    }

    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(Throwable cause) {
        super(cause);
    }
}
