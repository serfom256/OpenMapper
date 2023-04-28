package com.openmapper.core.strategy;

import com.openmapper.exceptions.EntityFieldAccessException;
import com.openmapper.exceptions.ObjectCreationException;
import com.openmapper.util.ResultSetObjectMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntityResultSetHandler implements ResultSetHandler<Object> {

    @Override
    public Object handle(ResultSet rs, Type mappingType) throws SQLException {
        try {
            return ResultSetObjectMapper.extractResult(mappingType, (Class<?>) mappingType, (Class<?>) mappingType, rs);
        } catch (EntityFieldAccessException e) {
            throw new RuntimeException(e);
        } catch (ObjectCreationException e) {
            throw new RuntimeException(e);
        }
    }
}
