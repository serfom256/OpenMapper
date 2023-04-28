package com.openmapper.core.strategy;

import com.openmapper.exceptions.EntityFieldAccessException;
import com.openmapper.exceptions.ObjectCreationException;
import com.openmapper.util.ResultSetObjectMapper;

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
            return (Iterable<Object>) ResultSetObjectMapper.extractResult(mappingType, entityMappedType, Iterable.class, rs);
        } catch (EntityFieldAccessException e) {
            throw new RuntimeException(e);
        } catch (ObjectCreationException e) {
            throw new RuntimeException(e);
        }

    }

}
