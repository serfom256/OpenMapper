package io.github.serfom256.core.query.handlers;

import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.exceptions.entity.EntityFieldAccessException;
import io.github.serfom256.exceptions.entity.EntityMappingException;
import io.github.serfom256.exceptions.entity.ObjectCreationException;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class IterableResultSetHandler implements ResultSetHandler<Iterable<Object>> {

    private final ResultSetObjectMapper objectMapper;
    private final ResultSetPrimitiveMapper primitiveMapper;

    public IterableResultSetHandler(ResultSetObjectMapper objectMapper, ResultSetPrimitiveMapper primitiveMapper) {
        this.objectMapper = objectMapper;
        this.primitiveMapper = primitiveMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterable<Object> handle(ResultSet rs, Type mappingType) throws SQLException {
        Class<?> entityMappedType = ((Class<?>) ((ParameterizedType) mappingType).getActualTypeArguments()[0]);
        try {
            Iterable<Object> result;
            if (entityMappedType.getAnnotation(Model.class) != null) {
                result = (Iterable<Object>) objectMapper.extract(mappingType, entityMappedType, Iterable.class, rs);
            } else {
                result = (Iterable<Object>) primitiveMapper.extract(mappingType, entityMappedType, Iterable.class, rs);
            }
            return result == null ? new ArrayList<>() : result;
        } catch (EntityFieldAccessException | ObjectCreationException e) {
            throw new EntityMappingException(e);
        }
    }
}
