package io.github.serfom256.core.query.mapping;

import io.github.serfom256.mappers.ResultSetUtils;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.sql.SQLException;

final class ScalarBinding {

    private final String fieldName;
    private final String column;
    private final MethodHandle setter;
    private final ResultSetUtils.ColumnExtractor extractor;
    private final ResultSetUtils.PositionalExtractor positional;
    private final Class<?> fieldType;

    ScalarBinding(String fieldName, String column, MethodHandle setter,
                  ResultSetUtils.ColumnExtractor extractor,
                  ResultSetUtils.PositionalExtractor positional,
                  Class<?> fieldType) {
        this.fieldName = fieldName;
        this.column = column;
        this.setter = setter;
        this.extractor = extractor;
        this.positional = positional;
        this.fieldType = fieldType;
    }

    Class<?> fieldType() {
        return fieldType;
    }

    Object defaultValue() {
        if (!fieldType.isPrimitive()) {
            return null;
        }
        if (fieldType == boolean.class) {
            return Boolean.FALSE;
        }
        if (fieldType == char.class) {
            return (char) 0;
        }
        if (fieldType == long.class) {
            return 0L;
        }
        if (fieldType == float.class) {
            return 0f;
        }
        if (fieldType == double.class) {
            return 0d;
        }
        if (fieldType == byte.class) {
            return (byte) 0;
        }
        if (fieldType == short.class) {
            return (short) 0;
        }
        return 0;
    }

    String fieldName() {
        return fieldName;
    }

    String column() {
        return column;
    }

    Object read(ResultSet resultSet) throws SQLException {
        return extractor.extract(resultSet, column);
    }

    Object read(ResultSet resultSet, int index) throws SQLException {
        if (positional != null && index != RowLayout.ABSENT) {
            return positional.extract(resultSet, index);
        }
        return extractor.extract(resultSet, column);
    }

    void apply(Object instance, ResultSet resultSet) throws SQLException {
        ReflectiveAccessors.set(setter, instance, extractor.extract(resultSet, column));
    }

    void apply(Object instance, ResultSet resultSet, int index) throws SQLException {
        ReflectiveAccessors.set(setter, instance, read(resultSet, index));
    }

    void applyValue(Object instance, Object value) {
        ReflectiveAccessors.set(setter, instance, value);
    }
}
