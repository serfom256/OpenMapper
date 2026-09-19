package io.github.serfom256.core.query.mapping;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

final class NestedBinding {

    private final MethodHandle setter;
    private final MethodHandle constructor;
    private final List<ScalarBinding> scalars;

    NestedBinding(MethodHandle setter, MethodHandle constructor, List<ScalarBinding> scalars) {
        this.setter = setter;
        this.constructor = constructor;
        this.scalars = scalars;
    }

    void apply(Object owner, ResultSet resultSet, Set<String> columns) throws SQLException {
        Object nested = ReflectiveAccessors.construct(constructor);
        for (ScalarBinding scalar : scalars) {
            if (columns.contains(scalar.column())) {
                scalar.apply(nested, resultSet);
            }
        }
        ReflectiveAccessors.set(setter, owner, nested);
    }
}
