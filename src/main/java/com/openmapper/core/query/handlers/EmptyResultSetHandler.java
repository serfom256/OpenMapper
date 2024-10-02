package com.openmapper.core.query.handlers;

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
