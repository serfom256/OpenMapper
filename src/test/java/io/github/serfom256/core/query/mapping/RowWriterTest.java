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

class RowWriterTest {

    private final MappingPlanRegistry registry =
            new MappingPlanRegistry(mock(Serializer.class), new ColumnTypeRegistry());

    @Test
    @DisplayName("writes every column present in the layout by its resolved index")
    void writesPresentColumnsByIndex() throws Exception {
        ResultSet resultSet = resultSetWith("ID", "COUNT", "RATIO", "ACTIVE", "NAME");
        when(resultSet.getLong(1)).thenReturn(7L);
        when(resultSet.getInt(2)).thenReturn(42);
        when(resultSet.getDouble(3)).thenReturn(1.5);
        when(resultSet.getBoolean(4)).thenReturn(true);
        when(resultSet.getString(5)).thenReturn("row");

        PrimitiveModel instance = new PrimitiveModel();
        writerFor(PrimitiveModel.class, resultSet).write(instance, resultSet, null, null);

        assertThat(instance.id).isEqualTo(7L);
        assertThat(instance.count).isEqualTo(42);
        assertThat(instance.ratio).isEqualTo(1.5);
        assertThat(instance.active).isTrue();
        assertThat(instance.name).isEqualTo("row");
    }

    @Test
    @DisplayName("leaves fields untouched when their column is absent from the layout")
    void skipsAbsentColumns() throws Exception {
        ResultSet resultSet = resultSetWith("ID", "NAME");
        when(resultSet.getLong(1)).thenReturn(3L);
        when(resultSet.getString(2)).thenReturn("partial");

        PrimitiveModel instance = new PrimitiveModel();
        writerFor(PrimitiveModel.class, resultSet).write(instance, resultSet, null, null);

        assertThat(instance.id).isEqualTo(3L);
        assertThat(instance.name).isEqualTo("partial");
        assertThat(instance.count).isZero();
        assertThat(instance.ratio).isZero();
        assertThat(instance.active).isFalse();
    }

    @Test
    @DisplayName("uses the known primary key instead of re-reading it")
    void prefersKnownPrimaryKey() throws Exception {
        ResultSet resultSet = resultSetWith("ID", "NAME");
        when(resultSet.getString(2)).thenReturn("named");

        MappingPlan plan = registry.planFor(PrimitiveModel.class);
        PrimitiveModel instance = new PrimitiveModel();
        RowLayout.of(resultSet).writerFor(scalarsOf(plan))
                .write(instance, resultSet, 99L, primaryKeyOf(plan));

        assertThat(instance.id).isEqualTo(99L);
        assertThat(instance.name).isEqualTo("named");
    }

    private RowWriter writerFor(Class<?> type, ResultSet resultSet) throws Exception {
        return RowLayout.of(resultSet).writerFor(scalarsOf(registry.planFor(type)));
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

    @SuppressWarnings("unchecked")
    private List<ScalarBinding> scalarsOf(MappingPlan plan) throws Exception {
        java.lang.reflect.Field field = MappingPlan.class.getDeclaredField("scalars");
        field.setAccessible(true);
        return (List<ScalarBinding>) field.get(plan);
    }

    private ScalarBinding primaryKeyOf(MappingPlan plan) throws Exception {
        java.lang.reflect.Field field = MappingPlan.class.getDeclaredField("primaryKey");
        field.setAccessible(true);
        return (ScalarBinding) field.get(plan);
    }

    @Model(primaryKey = "id")
    public static class PrimitiveModel {
        @Field(name = "ID")
        private long id;
        @Field(name = "COUNT")
        private int count;
        @Field(name = "RATIO")
        private double ratio;
        @Field(name = "ACTIVE")
        private boolean active;
        @Field(name = "NAME")
        private String name;
    }
}
