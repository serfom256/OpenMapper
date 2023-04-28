package com.openmapper.util;


import com.openmapper.core.strategy.ResultSetExtractor;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ResultSetUtils {

    private static final Map<Class<?>, ResultSetExtractor> RESULT_SET_TYPES = new HashMap<>(32);

    static {
        RESULT_SET_TYPES.put(Integer.class, ResultSet::getInt);
        RESULT_SET_TYPES.put(int.class, ResultSet::getInt);
        RESULT_SET_TYPES.put(Double.class, ResultSet::getDouble);
        RESULT_SET_TYPES.put(double.class, ResultSet::getDouble);
        RESULT_SET_TYPES.put(Long.class, ResultSet::getLong);
        RESULT_SET_TYPES.put(long.class, ResultSet::getLong);
        RESULT_SET_TYPES.put(Float.class, ResultSet::getFloat);
        RESULT_SET_TYPES.put(float.class, ResultSet::getFloat);
        RESULT_SET_TYPES.put(Short.class, ResultSet::getShort);
        RESULT_SET_TYPES.put(short.class, ResultSet::getShort);
        RESULT_SET_TYPES.put(Byte.class, ResultSet::getByte);
        RESULT_SET_TYPES.put(byte.class, ResultSet::getByte);
        RESULT_SET_TYPES.put(Boolean.class, ResultSet::getBoolean);
        RESULT_SET_TYPES.put(boolean.class, ResultSet::getBoolean);

        RESULT_SET_TYPES.put(String.class, ResultSet::getString);
        RESULT_SET_TYPES.put(Object.class, ResultSet::getObject);

        RESULT_SET_TYPES.put(Date.class, ResultSet::getDate);
        RESULT_SET_TYPES.put(Time.class, ResultSet::getTime);
        RESULT_SET_TYPES.put(Timestamp.class, ResultSet::getTimestamp);

        RESULT_SET_TYPES.put(BigDecimal.class, ResultSet::getBigDecimal);
        RESULT_SET_TYPES.put(Blob.class, ResultSet::getBlob);
    }

    public static Object extractFromResultSet(Class<?> returnType, ResultSet resultSet, String columnName) {
        try {
            return RESULT_SET_TYPES.get(returnType).extract(resultSet, columnName);
        } catch (SQLException e) {
            return null;
        }
    }

    public static ResultSetExtractor getType(Class<?> classType) {
        ResultSetExtractor resultSetExtractor = RESULT_SET_TYPES.get(classType);
        if (resultSetExtractor == null) {
            throw new RuntimeException("Specified type doesn't supported");
        }
        return resultSetExtractor;
    }
}
