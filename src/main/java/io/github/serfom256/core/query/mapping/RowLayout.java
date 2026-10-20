package io.github.serfom256.core.query.mapping;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

final class RowLayout {

    static final int ABSENT = 0;

    private final Map<String, Integer> indexByLabel;
    private final IdentityHashMap<List<ScalarBinding>, int[]> resolved = new IdentityHashMap<>();
    private final IdentityHashMap<List<ScalarBinding>, RowWriter> writers = new IdentityHashMap<>();

    private RowLayout(Map<String, Integer> indexByLabel) {
        this.indexByLabel = indexByLabel;
    }

    static RowLayout of(ResultSet resultSet) throws SQLException {
        ResultSetMetaData meta = resultSet.getMetaData();
        int count = meta.getColumnCount();
        Map<String, Integer> byLabel = new HashMap<>(Math.max(16, count * 2));
        for (int i = 1; i <= count; i++) {
            byLabel.putIfAbsent(meta.getColumnLabel(i), i);
        }
        return new RowLayout(byLabel);
    }

    boolean has(String column) {
        return column != null && indexByLabel.containsKey(column);
    }

    int indexOf(String column) {
        if (column == null) {
            return ABSENT;
        }
        Integer index = indexByLabel.get(column);
        return index == null ? ABSENT : index;
    }

    RowWriter writerFor(List<ScalarBinding> scalars) {
        RowWriter cached = writers.get(scalars);
        if (cached != null) {
            return cached;
        }
        RowWriter writer = RowWriter.build(scalars, indicesOf(scalars));
        writers.put(scalars, writer);
        return writer;
    }

    int[] indicesOf(List<ScalarBinding> scalars) {
        int[] cached = resolved.get(scalars);
        if (cached != null) {
            return cached;
        }
        int[] indices = new int[scalars.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indexOf(scalars.get(i).column());
        }
        resolved.put(scalars, indices);
        return indices;
    }
}
