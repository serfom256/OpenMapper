package io.github.serfom256.mappers;

import io.github.serfom256.exceptions.common.UnsupportedTypeException;
import io.github.serfom256.exceptions.internal.InvalidResultCountException;
import io.github.serfom256.exceptions.internal.ResultSetExtractionException;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class ResultSetUtils {

    private ResultSetUtils() {
    }

    @FunctionalInterface
    private interface ResultSetExtractorByColumn {
        Object extract(ResultSet resultSet, String name) throws SQLException;
    }

    @FunctionalInterface
    public interface ColumnExtractor {
        Object extract(ResultSet resultSet, String column) throws SQLException;
    }

    public static ColumnExtractor extractorForColumn(Class<?> type) {
        ResultSetExtractorByColumn extractor = RESULT_SET_TYPES_BY_NAME.get(type);
        if (extractor == null) {
            throw new UnsupportedTypeException(type.getTypeName());
        }
        return extractor::extract;
    }

    @FunctionalInterface
    private interface ResultSetExtractorByPosition {
        Object extract(ResultSet resultSet, int position) throws SQLException;
    }

    private static final Map<Class<?>, ResultSetExtractorByColumn> RESULT_SET_TYPES_BY_NAME = new HashMap<>(32);
    private static final Map<Class<?>, ResultSetExtractorByPosition> RESULT_SET_TYPES_BY_POSITION = new HashMap<>(32);

    static {
        RESULT_SET_TYPES_BY_NAME.put(Integer.class, nullableByName(ResultSet::getInt));
        RESULT_SET_TYPES_BY_NAME.put(int.class, ResultSet::getInt);
        RESULT_SET_TYPES_BY_NAME.put(Double.class, nullableByName(ResultSet::getDouble));
        RESULT_SET_TYPES_BY_NAME.put(double.class, ResultSet::getDouble);
        RESULT_SET_TYPES_BY_NAME.put(Long.class, nullableByName(ResultSet::getLong));
        RESULT_SET_TYPES_BY_NAME.put(long.class, ResultSet::getLong);
        RESULT_SET_TYPES_BY_NAME.put(Float.class, nullableByName(ResultSet::getFloat));
        RESULT_SET_TYPES_BY_NAME.put(float.class, ResultSet::getFloat);
        RESULT_SET_TYPES_BY_NAME.put(Short.class, nullableByName(ResultSet::getShort));
        RESULT_SET_TYPES_BY_NAME.put(short.class, ResultSet::getShort);
        RESULT_SET_TYPES_BY_NAME.put(Byte.class, nullableByName(ResultSet::getByte));
        RESULT_SET_TYPES_BY_NAME.put(byte.class, ResultSet::getByte);
        RESULT_SET_TYPES_BY_NAME.put(Boolean.class, nullableByName(ResultSet::getBoolean));
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
        RESULT_SET_TYPES_BY_POSITION.put(Integer.class, nullableByPosition(ResultSet::getInt));
        RESULT_SET_TYPES_BY_POSITION.put(int.class, ResultSet::getInt);
        RESULT_SET_TYPES_BY_POSITION.put(Double.class, nullableByPosition(ResultSet::getDouble));
        RESULT_SET_TYPES_BY_POSITION.put(double.class, ResultSet::getDouble);
        RESULT_SET_TYPES_BY_POSITION.put(Long.class, nullableByPosition(ResultSet::getLong));
        RESULT_SET_TYPES_BY_POSITION.put(long.class, ResultSet::getLong);
        RESULT_SET_TYPES_BY_POSITION.put(Float.class, nullableByPosition(ResultSet::getFloat));
        RESULT_SET_TYPES_BY_POSITION.put(float.class, ResultSet::getFloat);
        RESULT_SET_TYPES_BY_POSITION.put(Short.class, nullableByPosition(ResultSet::getShort));
        RESULT_SET_TYPES_BY_POSITION.put(short.class, ResultSet::getShort);
        RESULT_SET_TYPES_BY_POSITION.put(Byte.class, nullableByPosition(ResultSet::getByte));
        RESULT_SET_TYPES_BY_POSITION.put(byte.class, ResultSet::getByte);
        RESULT_SET_TYPES_BY_POSITION.put(Boolean.class, nullableByPosition(ResultSet::getBoolean));
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

    private static ResultSetExtractorByColumn nullableByName(ResultSetExtractorByColumn extractor) {
        return (resultSet, column) -> {
            Object value = extractor.extract(resultSet, column);
            return resultSet.wasNull() ? null : value;
        };
    }

    private static ResultSetExtractorByPosition nullableByPosition(ResultSetExtractorByPosition extractor) {
        return (resultSet, position) -> {
            Object value = extractor.extract(resultSet, position);
            return resultSet.wasNull() ? null : value;
        };
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
            throw new InvalidResultCountException(
                    "Invalid result set length. Expected 1 got: " + resultSet.getMetaData().getColumnCount());
        }
    }
}
