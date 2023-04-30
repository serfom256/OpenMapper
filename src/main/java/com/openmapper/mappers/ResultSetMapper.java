package com.openmapper.mappers;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper {

    Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws SQLException;
}
