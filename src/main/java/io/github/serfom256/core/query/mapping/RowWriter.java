package io.github.serfom256.core.query.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

final class RowWriter {

    private final ScalarBinding[] scalars;
    private final int[] indices;

    private RowWriter(ScalarBinding[] scalars, int[] indices) {
        this.scalars = scalars;
        this.indices = indices;
    }

    static RowWriter build(List<ScalarBinding> scalars, int[] indices) {
        ScalarBinding[] present = new ScalarBinding[indices.length];
        int[] presentIndices = new int[indices.length];
        int count = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == RowLayout.ABSENT) {
                continue;
            }
            present[count] = scalars.get(i);
            presentIndices[count] = indices[i];
            count++;
        }
        return new RowWriter(Arrays.copyOf(present, count), Arrays.copyOf(presentIndices, count));
    }

    void write(Object instance, ResultSet resultSet, Object knownPrimaryKey, ScalarBinding primaryKey)
            throws SQLException {
        for (int i = 0; i < scalars.length; i++) {
            ScalarBinding scalar = scalars[i];
            if (knownPrimaryKey != null && scalar == primaryKey) {
                scalar.applyValue(instance, knownPrimaryKey);
            } else {
                scalar.apply(instance, resultSet, indices[i]);
            }
        }
    }
}
