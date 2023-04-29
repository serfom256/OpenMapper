package com.openmapper.core.strategy.impl;

import com.openmapper.core.strategy.ResultSetHandler;

import java.lang.reflect.Type;
import java.sql.ResultSet;

public class EmptyResultSetHandler implements ResultSetHandler<Void> {

    @Override
    public Void handle(ResultSet rs, Type mappingType) {
        return null;
    }
}
