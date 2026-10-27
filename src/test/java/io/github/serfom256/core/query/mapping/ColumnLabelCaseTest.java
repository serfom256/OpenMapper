package io.github.serfom256.core.query.mapping;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.json.Serializer;
import io.github.serfom256.mappers.ColumnTypeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ColumnLabelCaseTest {

    private final MappingPlanRegistry registry =
            new MappingPlanRegistry(mock(Serializer.class), new ColumnTypeRegistry());

    @Test
    @DisplayName("columns resolve when the driver uppercases labels, as Oracle and H2 do")
    void resolvesUppercaseLabels() throws Exception {
        ResultSet resultSet = resultSetWith("ID", "NAME");
        when(resultSet.getLong(1)).thenReturn(5L);
        when(resultSet.getString(2)).thenReturn("upper");

        Item item = new Item();
        writerFor(resultSet).write(item, resultSet, null, null);

        assertThat(item.id).isEqualTo(5L);
        assertThat(item.name).isEqualTo("upper");
    }

    @Test
    @DisplayName("columns resolve when the driver lowercases labels, as PostgreSQL does")
    void resolvesLowercaseLabels() throws Exception {
        ResultSet resultSet = resultSetWith("id", "name");
        when(resultSet.getLong(1)).thenReturn(6L);
        when(resultSet.getString(2)).thenReturn("lower");

        Item item = new Item();
        writerFor(resultSet).write(item, resultSet, null, null);

        assertThat(item.id).isEqualTo(6L);
        assertThat(item.name).isEqualTo("lower");
    }

    @Test
    @DisplayName("the primary key column is found regardless of label case")
    void findsPrimaryKeyRegardlessOfCase() throws Exception {
        RowLayout layout = RowLayout.of(resultSetWith("ID", "NAME"));

        assertThat(layout.has("id")).isTrue();
        assertThat(layout.indexOf("id")).isEqualTo(1);
        assertThat(layout.indexOf("Name")).isEqualTo(2);
    }

    private RowWriter writerFor(ResultSet resultSet) throws Exception {
        MappingPlan plan = registry.planFor(Item.class);
        java.lang.reflect.Field field = MappingPlan.class.getDeclaredField("scalars");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<ScalarBinding> scalars = (List<ScalarBinding>) field.get(plan);
        return RowLayout.of(resultSet).writerFor(scalars);
    }

    private ResultSet resultSetWith(String... columns) throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData meta = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(meta);
        when(meta.getColumnCount()).thenReturn(columns.length);
        for (int i = 0; i < columns.length; i++) {
            when(meta.getColumnLabel(i + 1)).thenReturn(columns[i]);
        }
        return resultSet;
    }

    @Model(primaryKey = "id")
    public static class Item {
        @Field(name = "id")
        private long id;
        @Field(name = "name")
        private String name;
    }
}
