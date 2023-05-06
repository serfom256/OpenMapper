package com.openmapper.mappers;

import com.openmapper.exceptions.internal.QueryExecutionError;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResultSetPrimitiveMapper implements ResultSetMapper {

    @Override
    public Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws SQLException {
        try {
            List<Object> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(ResultSetUtils.getFirst(actualReturnType, resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new QueryExecutionError(e);
        }
    }
}
