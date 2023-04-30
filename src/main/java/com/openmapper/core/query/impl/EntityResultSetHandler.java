package com.openmapper.core.query.impl;

import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.query.ResultSetHandler;
import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.EntityMappingException;
import com.openmapper.exceptions.entity.ObjectCreationException;
import com.openmapper.mappers.ResultSetObjectMapper;
import com.openmapper.mappers.ResultSetPrimitiveMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class EntityResultSetHandler implements ResultSetHandler<Object> {

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(ResultSet rs, Type mappingType) throws SQLException {
        try {
            if (mappingType.getClass().getAnnotation(Entity.class) != null) {
                return new ResultSetObjectMapper().extract(mappingType, (Class<?>) mappingType, (Class<?>) mappingType, rs);
            }
            List<Object> rows = (List<Object>) new ResultSetPrimitiveMapper().extract(mappingType, (Class<?>) mappingType, (Class<?>) mappingType, rs);
            if (rows.size() > 1) {
                throw new TooManyResultsFoundException("Expected single result got: " + rows.size());
            }
            return rows.get(0);
        } catch (EntityFieldAccessException | ObjectCreationException e) {
            throw new EntityMappingException(e);
        }
    }
}
