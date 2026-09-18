package io.github.serfom256.mappers;

import io.github.serfom256.common.reflect.ObjectUtils;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps a result set to an entity (or list of entities) by delegating to the
 * single-pass {@link ResultSetGraphReader}. This class only adapts the
 * {@link ResultSetMapper} contract to the reader.
 */
public class ResultSetObjectMapper implements ResultSetMapper {

    private final ResultSetGraphReader graphReader;

    public ResultSetObjectMapper(ResultSetGraphReader graphReader) {
        this.graphReader = graphReader;
    }

    @Override
    public Object extract(Type concreteType, Class<?> returnClass, Class<?> actualReturnClass, ResultSet resultSet)
            throws SQLException {
        Class<?> rootType = ObjectUtils.getInnerClassType(returnClass, concreteType);
        boolean iterable = ObjectUtils.isIterable(actualReturnClass);
        return graphReader.map(rootType, iterable, resultSet);
    }
}
