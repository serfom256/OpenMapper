package com.openmapper.core.query.handlers;

import com.openmapper.annotations.entity.Model;
import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.EntityMappingException;
import com.openmapper.exceptions.entity.ObjectCreationException;
import com.openmapper.mappers.ResultSetObjectMapper;
import com.openmapper.mappers.ResultSetPrimitiveMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Component
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
