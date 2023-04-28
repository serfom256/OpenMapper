package com.openmapper.core.strategy;

import java.lang.reflect.Type;
import java.sql.ResultSet;

public class EmptyResultSetHandler implements ResultSetHandler<Void> {

    @Override
    public Void handle(ResultSet rs, Type mappingType) {
        return null;
    }
}
