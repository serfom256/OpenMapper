package com.openmapper.mappers.entity;


import com.openmapper.exceptions.internal.InvalidResultCountException;
import com.openmapper.exceptions.internal.ResultSetExtractionException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ResultSetUtils {

    @FunctionalInterface
    private static interface ResultSetExtractorByColumn {
        Object extract(ResultSet resultSet, String name) throws SQLException;
    }

    @FunctionalInterface
    private static interface ResultSetExtractorByPosition {
        Object extract(ResultSet resultSet, int position) throws SQLException;
    }

    private static final Map<Class<?>, ResultSetExtractorByColumn> RESULT_SET_TYPES_BY_NAME = new HashMap<>(32);
    private static final Map<Class<?>, ResultSetExtractorByPosition> RESULT_SET_TYPES_BY_POSITION = new HashMap<>(32);

    static {
        RESULT_SET_TYPES_BY_NAME.put(Integer.class, ResultSet::getInt);
        RESULT_SET_TYPES_BY_NAME.put(int.class, ResultSet::getInt);
        RESULT_SET_TYPES_BY_NAME.put(Double.class, ResultSet::getDouble);
        RESULT_SET_TYPES_BY_NAME.put(double.class, ResultSet::getDouble);
        RESULT_SET_TYPES_BY_NAME.put(Long.class, ResultSet::getLong);
        RESULT_SET_TYPES_BY_NAME.put(long.class, ResultSet::getLong);
        RESULT_SET_TYPES_BY_NAME.put(Float.class, ResultSet::getFloat);
        RESULT_SET_TYPES_BY_NAME.put(float.class, ResultSet::getFloat);
        RESULT_SET_TYPES_BY_NAME.put(Short.class, ResultSet::getShort);
        RESULT_SET_TYPES_BY_NAME.put(short.class, ResultSet::getShort);
        RESULT_SET_TYPES_BY_NAME.put(Byte.class, ResultSet::getByte);
        RESULT_SET_TYPES_BY_NAME.put(byte.class, ResultSet::getByte);
        RESULT_SET_TYPES_BY_NAME.put(Boolean.class, ResultSet::getBoolean);
        RESULT_SET_TYPES_BY_NAME.put(boolean.class, ResultSet::getBoolean);

        RESULT_SET_TYPES_BY_NAME.put(String.class, ResultSet::getString);
        RESULT_SET_TYPES_BY_NAME.put(Object.class, ResultSet::getObject);

        RESULT_SET_TYPES_BY_NAME.put(Date.class, ResultSet::getDate);
        RESULT_SET_TYPES_BY_NAME.put(Time.class, ResultSet::getTime);
        RESULT_SET_TYPES_BY_NAME.put(Timestamp.class, ResultSet::getTimestamp);

        RESULT_SET_TYPES_BY_NAME.put(BigDecimal.class, ResultSet::getBigDecimal);
        RESULT_SET_TYPES_BY_NAME.put(Blob.class, ResultSet::getBlob);
    }

    static {
        RESULT_SET_TYPES_BY_POSITION.put(Integer.class, ResultSet::getInt);
        RESULT_SET_TYPES_BY_POSITION.put(int.class, ResultSet::getInt);
        RESULT_SET_TYPES_BY_POSITION.put(Double.class, ResultSet::getDouble);
        RESULT_SET_TYPES_BY_POSITION.put(double.class, ResultSet::getDouble);
        RESULT_SET_TYPES_BY_POSITION.put(Long.class, ResultSet::getLong);
        RESULT_SET_TYPES_BY_POSITION.put(long.class, ResultSet::getLong);
        RESULT_SET_TYPES_BY_POSITION.put(Float.class, ResultSet::getFloat);
        RESULT_SET_TYPES_BY_POSITION.put(float.class, ResultSet::getFloat);
        RESULT_SET_TYPES_BY_POSITION.put(Short.class, ResultSet::getShort);
        RESULT_SET_TYPES_BY_POSITION.put(short.class, ResultSet::getShort);
        RESULT_SET_TYPES_BY_POSITION.put(Byte.class, ResultSet::getByte);
        RESULT_SET_TYPES_BY_POSITION.put(byte.class, ResultSet::getByte);
        RESULT_SET_TYPES_BY_POSITION.put(Boolean.class, ResultSet::getBoolean);
        RESULT_SET_TYPES_BY_POSITION.put(boolean.class, ResultSet::getBoolean);

        RESULT_SET_TYPES_BY_POSITION.put(String.class, ResultSet::getString);
        RESULT_SET_TYPES_BY_POSITION.put(Object.class, ResultSet::getObject);

        RESULT_SET_TYPES_BY_POSITION.put(Date.class, ResultSet::getDate);
        RESULT_SET_TYPES_BY_POSITION.put(Time.class, ResultSet::getTime);
        RESULT_SET_TYPES_BY_POSITION.put(Timestamp.class, ResultSet::getTimestamp);

        RESULT_SET_TYPES_BY_POSITION.put(BigDecimal.class, ResultSet::getBigDecimal);
        RESULT_SET_TYPES_BY_POSITION.put(Blob.class, ResultSet::getBlob);
    }

    public static Object extractFromResultSet(Class<?> returnType, ResultSet resultSet, String columnName) {
        try {
            return RESULT_SET_TYPES_BY_NAME.get(returnType).extract(resultSet, columnName);
        } catch (SQLException e) {
            throw new ResultSetExtractionException(e);
        }
    }

    public static Object getFirst(Class<?> returnType, ResultSet resultSet) {
        try {
            checkConstraints(resultSet);
            return RESULT_SET_TYPES_BY_POSITION.get(returnType).extract(resultSet, 1);
        } catch (SQLException e) {
            throw new ResultSetExtractionException(e);
        }
    }

    private static void checkConstraints(ResultSet resultSet) throws SQLException {
        if (resultSet.getMetaData().getColumnCount() != 1) {
            throw new InvalidResultCountException("Invalid result set length. Expected 1 got: " + resultSet.getMetaData().getColumnCount());
        }
    }
}
