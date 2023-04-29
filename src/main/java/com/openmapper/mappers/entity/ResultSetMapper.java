package com.openmapper.mappers.entity;

import com.openmapper.exceptions.entity.EntityFieldAccessException;
import com.openmapper.exceptions.entity.ObjectCreationException;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper {

    Object extract(Type concreteType, Class<?> actualReturnType, Class<?> returnClass, ResultSet resultSet) throws SQLException, EntityFieldAccessException, ObjectCreationException;
}
