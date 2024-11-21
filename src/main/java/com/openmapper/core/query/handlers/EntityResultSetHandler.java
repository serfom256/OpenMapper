package com.openmapper.core.query.handlers;

import com.openmapper.annotations.entity.Model;
import com.openmapper.exceptions.internal.TooManyResultsFoundException;
import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.EntityMappingException;
import com.openmapper.exceptions.entity.ObjectCreationException;
import com.openmapper.mappers.ResultSetObjectMapper;
import com.openmapper.mappers.ResultSetPrimitiveMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class EntityResultSetHandler implements ResultSetHandler<Object> {

    private final ResultSetObjectMapper objectMapper;
    private final ResultSetPrimitiveMapper primitiveMapper;

    public EntityResultSetHandler(ResultSetObjectMapper objectMapper, ResultSetPrimitiveMapper primitiveMapper) {
        this.objectMapper = objectMapper;
        this.primitiveMapper = primitiveMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object handle(ResultSet rs, Type mappingType) throws SQLException {
        try {
            if (((Class<?>) mappingType).getAnnotation(Model.class) != null) {
                return objectMapper.extract(mappingType, (Class<?>) mappingType, (Class<?>) mappingType, rs);
            }
            List<Object> rows = (List<Object>) primitiveMapper.extract(mappingType, (Class<?>) mappingType,
                    (Class<?>) mappingType, rs);
            if (rows.size() > 1) {
                throw new TooManyResultsFoundException("Expected single result got: " + rows.size());
            }
            return rows.isEmpty() ? null : rows.get(0);
        } catch (EntityFieldAccessException | ObjectCreationException e) {
            throw new EntityMappingException(e);
        }
    }
}
