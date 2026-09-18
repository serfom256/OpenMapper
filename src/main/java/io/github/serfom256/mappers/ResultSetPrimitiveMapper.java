package io.github.serfom256.mappers;

import io.github.serfom256.exceptions.internal.QueryExecutionError;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetPrimitiveMapper implements ResultSetMapper {

    @Override
    public Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) {
        try {
            final List<Object> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(ResultSetUtils.getFirst(actualReturnType, resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
