package io.github.serfom256.core.query.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

public final class MappingPlan {

    private final Class<?> type;
    private final java.lang.invoke.MethodHandle constructor;
    private final boolean constructsCanonically;
    private final ScalarBinding primaryKey;
    private final List<ScalarBinding> scalars;
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

    Object readPrimaryKey(ResultSet resultSet) throws SQLException {
        Object value = primaryKey.read(resultSet);
        return resultSet.wasNull() ? null : value;
    }

    Object instantiate(ResultSet resultSet, Set<String> columns) throws SQLException {
        if (constructsCanonically) {
            return constructCanonically(resultSet, columns);
        }
        Object instance = ReflectiveAccessors.construct(constructor);
        for (ScalarBinding scalar : scalars) {
            if (columns.contains(scalar.column())) {
                scalar.apply(instance, resultSet);
            }
        }
        return instance;
    }

    private Object constructCanonically(ResultSet resultSet, Set<String> columns) throws SQLException {
        Object[] arguments = new Object[scalars.size()];
        for (int i = 0; i < arguments.length; i++) {
            ScalarBinding scalar = scalars.get(i);
            arguments[i] = columns.contains(scalar.column())
                    ? scalar.read(resultSet)
                    : scalar.defaultValue();
        }
        return ReflectiveAccessors.construct(constructor, arguments);
    }
}
