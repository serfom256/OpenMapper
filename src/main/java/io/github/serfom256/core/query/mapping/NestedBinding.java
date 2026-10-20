package io.github.serfom256.core.query.mapping;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

final class NestedBinding {

    private final MethodHandle setter;
    private final MethodHandle constructor;
    private final List<ScalarBinding> scalars;

    NestedBinding(MethodHandle setter, MethodHandle constructor, List<ScalarBinding> scalars) {
        this.setter = setter;
        this.constructor = constructor;
        this.scalars = scalars;
    }

    RowWriter writerFor(RowLayout layout) {
        return layout.writerFor(scalars);
    }

    void apply(Object owner, ResultSet resultSet, RowWriter writer) throws SQLException {
        Object nested = ReflectiveAccessors.construct(constructor);
        writer.write(nested, resultSet, null, null);
        ReflectiveAccessors.set(setter, owner, nested);
    }
}
