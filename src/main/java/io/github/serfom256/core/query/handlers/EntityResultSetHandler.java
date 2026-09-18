package io.github.serfom256.core.query.handlers;

import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.exceptions.internal.TooManyResultsFoundException;
import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.EntityMappingException;
import io.github.serfom256.exceptions.entity.ObjectCreationException;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
