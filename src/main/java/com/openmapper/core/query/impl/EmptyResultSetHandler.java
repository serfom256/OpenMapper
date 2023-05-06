package com.openmapper.core.query.impl;

import com.openmapper.core.query.ResultSetHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.ResultSet;

@Component
public class EmptyResultSetHandler implements ResultSetHandler<Void> {

    @Override
    public Void handle(ResultSet rs, Type mappingType) {
        return null;
    }
}
