package io.github.serfom256.unit.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.exceptions.entity.ModelValidationException;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.kotlininterop.ProductBean;
import io.github.serfom256.kotlininterop.ProductData;

/**
 * Establishes which Kotlin class shapes the mapper can materialise today.
 */
class KotlinInteropTest {

    private final ResultSetGraphReader reader = new ResultSetGraphReader(new MappingPlanRegistry(null));

    private ResultSet oneRow() throws SQLException {
        ResultSet rs = Mockito.mock(ResultSet.class);
        ResultSetMetaData md = Mockito.mock(ResultSetMetaData.class);
        String[] columns = {"id", "name", "price"};
        Mockito.when(rs.getMetaData()).thenReturn(md);
        Mockito.when(md.getColumnCount()).thenReturn(columns.length);
        for (int i = 0; i < columns.length; i++) {
            Mockito.when(md.getColumnLabel(i + 1)).thenReturn(columns[i]);
        }
        Mockito.when(rs.next()).thenReturn(true, false);
        Mockito.when(rs.getLong("id")).thenReturn(7L);
        Mockito.when(rs.getString("name")).thenReturn("widget");
        Mockito.when(rs.getDouble("price")).thenReturn(1.5);
        Mockito.when(rs.getObject("id")).thenReturn(7L);
        return rs;
    }

    @Test
    @DisplayName("Kotlin class with var properties maps like a Java POJO")
    void mutableKotlinClassMaps() throws SQLException {
        ProductBean product = (ProductBean) reader.map(ProductBean.class, false, oneRow());

        assertNotNull(product);
        assertEquals(7L, product.getId());
        assertEquals("widget", product.getName());
    }

    @Model(primaryKey = "id")
    public record ProductRecord(
            @Field(name = "id") long id,
            @Field(name = "name") String name,
            @Field(name = "price") double price) {
    }

    @Test
    @DisplayName("Kotlin data class is built through its primary constructor")
    void immutableDataClassMaps() throws SQLException {
        ProductData product = (ProductData) reader.map(ProductData.class, false, oneRow());

        assertNotNull(product);
        assertEquals(7L, product.getId());
        assertEquals("widget", product.getName());
        assertEquals(1.5, product.getPrice());
    }

    @Test
    @DisplayName("Java record is built through its canonical constructor")
    void recordMaps() throws SQLException {
        ProductRecord product = (ProductRecord) reader.map(ProductRecord.class, false, oneRow());

        assertNotNull(product);
        assertEquals(7L, product.id());
        assertEquals("widget", product.name());
        assertEquals(1.5, product.price());
    }

    @Model(primaryKey = "id")
    public record PartialRecord(
            @Field(name = "id") long id,
            @Field(name = "name") String name,
            @Field(name = "missing") String missing) {
    }

    @Test
    @DisplayName("an unselected column yields the component's default, it cannot be skipped")
    void unselectedColumnGetsDefault() throws SQLException {
        PartialRecord product = (PartialRecord) reader.map(PartialRecord.class, false, oneRow());

        assertEquals(7L, product.id());
        assertEquals("widget", product.name());
        assertNull(product.missing());
    }

    @Model(primaryKey = "id")
    public record JoinedRecord(
            @Field(name = "id") long id,
            @Joined(joinBy = "id", to = "productId") java.util.List<ProductRecord> items) {
    }

    @Test
    @DisplayName("@Joined on an immutable type is rejected with an explanation")
    void joinedOnImmutableTypeIsRejected() {
        ModelValidationException thrown = assertThrows(ModelValidationException.class,
                () -> reader.map(JoinedRecord.class, false, oneRow()));
        assertTrue(thrown.getMessage().contains("mutable type"));
    }
}
