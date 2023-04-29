package com.openmapper.core.strategy.impl;

import com.openmapper.core.annotations.entity.Entity;
import com.openmapper.core.strategy.ResultSetHandler;
import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.EntityMappingException;
import com.openmapper.exceptions.entity.ObjectCreationException;
import com.openmapper.mappers.entity.ResultSetObjectMapper;
import com.openmapper.mappers.entity.ResultSetPrimitiveMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IterableResultSetHandler implements ResultSetHandler<Iterable<Object>> {

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Object> handle(ResultSet rs, Type mappingType) throws SQLException {
        Class<?> entityMappedType = ((Class<?>) ((ParameterizedType) mappingType).getActualTypeArguments()[0]);
        try {
            if (entityMappedType.getAnnotation(Entity.class) != null) {
                return (Iterable<Object>) new ResultSetObjectMapper().extract(mappingType, entityMappedType, Iterable.class, rs);
            }
            return (Iterable<Object>) new ResultSetPrimitiveMapper().extract(mappingType, entityMappedType, Iterable.class, rs);
        } catch (EntityFieldAccessException | ObjectCreationException e) {
            throw new EntityMappingException(e);
        }
    }

}
