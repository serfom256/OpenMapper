package io.github.serfom256.core.query.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public final class MappingPlan {

    private final Class<?> type;
    private final java.lang.invoke.MethodHandle constructor;
    private final boolean constructsCanonically;
    private final ScalarBinding primaryKey;
    private final List<ScalarBinding> scalars;
    private final ScalarBinding[] scalarArray;
    private final List<NestedBinding> nested;
    private final List<AssociationBinding> associations;
    private final Map<String, String> columnByField;
    private final @Nullable String discriminatorColumn;
    private final Map<String, Class<?>> subtypes;

    MappingPlan(Class<?> type,
                java.lang.invoke.MethodHandle constructor,
                boolean constructsCanonically,
                ScalarBinding primaryKey,
                List<ScalarBinding> scalars,
                List<NestedBinding> nested,
                List<AssociationBinding> associations,
                @Nullable String discriminatorColumn,
                Map<String, Class<?>> subtypes) {
        this.type = type;
        this.discriminatorColumn = discriminatorColumn;
        this.subtypes = Map.copyOf(subtypes);
        this.constructor = constructor;
        this.constructsCanonically = constructsCanonically;
        this.primaryKey = primaryKey;
        this.scalars = scalars;
        this.scalarArray = scalars.toArray(new ScalarBinding[0]);
        this.nested = nested;
        this.associations = associations;
        Map<String, String> byField = new HashMap<>();
        for (ScalarBinding scalar : scalars) {
            byField.put(scalar.fieldName(), scalar.column());
        }
        this.columnByField = Map.copyOf(byField);
    }

    @Nullable String discriminatorColumn() {
        return discriminatorColumn;
    }

    @Nullable Class<?> subtypeFor(String discriminatorValue) {
        return subtypes.get(discriminatorValue);
    }

    Collection<Class<?>> subtypes() {
        return subtypes.values();
    }

    String columnForField(String fieldName) {
        return columnByField.get(fieldName);
    }

    Class<?> type() {
        return type;
    }

    String primaryKeyColumn() {
        return primaryKey.column();
    }

    List<NestedBinding> nested() {
        return nested;
    }

    List<AssociationBinding> associations() {
        return associations;
    }

    boolean hasNumericPrimaryKey() {
        Class<?> type = primaryKey.fieldType();
        return type == long.class || type == int.class || type == short.class || type == byte.class
                || type == Long.class || type == Integer.class || type == Short.class || type == Byte.class;
    }

    RowWriter writerFor(RowLayout layout) {
        return layout.writerFor(scalars);
    }

    int[] indicesFor(RowLayout layout) {
        return layout.indicesOf(scalars);
    }

    long readPrimaryKeyLong(ResultSet resultSet, int index) throws SQLException {
        return index == RowLayout.ABSENT
                ? resultSet.getLong(primaryKey.column())
                : resultSet.getLong(index);
    }

    Object readPrimaryKey(ResultSet resultSet, int index) throws SQLException {
        Object value = primaryKey.read(resultSet, index);
        return resultSet.wasNull() ? null : value;
    }

    Object instantiate(ResultSet resultSet, RowWriter writer, int[] indices, Object knownPrimaryKey)
            throws SQLException {
        if (constructsCanonically) {
            return constructCanonically(resultSet, indices, knownPrimaryKey);
        }
        Object instance = ReflectiveAccessors.construct(constructor);
        writer.write(instance, resultSet, knownPrimaryKey, primaryKey);
        return instance;
    }

    private Object constructCanonically(ResultSet resultSet, int[] indices, Object knownPrimaryKey)
            throws SQLException {
        Object[] arguments = new Object[scalarArray.length];
        for (int i = 0; i < arguments.length; i++) {
            ScalarBinding scalar = scalarArray[i];
            if (indices[i] == RowLayout.ABSENT) {
                arguments[i] = scalar.defaultValue();
            } else if (knownPrimaryKey != null && scalar == primaryKey) {
                arguments[i] = knownPrimaryKey;
            } else {
                arguments[i] = scalar.read(resultSet, indices[i]);
            }
        }
        return ReflectiveAccessors.construct(constructor, arguments);
    }
}
