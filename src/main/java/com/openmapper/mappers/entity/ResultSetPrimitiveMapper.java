package com.openmapper.mappers.entity;

import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.ObjectCreationException;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResultSetPrimitiveMapper implements ResultSetMapper {

    @Override
    public Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws EntityFieldAccessException, ObjectCreationException {
        try {
            List<Object> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(ResultSetUtils.getFirst(actualReturnType, resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
