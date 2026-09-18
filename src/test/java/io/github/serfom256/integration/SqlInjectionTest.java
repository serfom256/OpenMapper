package io.github.serfom256.integration;

import io.github.serfom256.benchmark.model.om.ProductOM;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end proof that string parameters are bound, not concatenated. Before
 * the
 * parameterised-query fix, an injection payload in a String parameter altered
 * the
 * executed SQL (e.g. returning every row); now it is treated as a literal
 * value.
 */
class SqlInjectionTest extends IntegrationTestBase {

    @Test
    void findByName_returnsOnlyTheMatchingRow() {
        List<ProductOM> result = productDao.findByName("Product-0");
        assertFalse(result.isEmpty());
        for (ProductOM p : result) {
            assertEquals("Product-0", p.getName());
        }
    }

    @Test
    void findByName_injectionPayload_isTreatedAsLiteral() {
        // Classic "OR '1'='1" auth-bypass payload. With string concatenation this
        // returned the entire table; with bind parameters it matches a literal name
        // that does not exist, so it returns nothing.
        List<ProductOM> result = productDao.findByName("x' OR '1'='1");
        assertTrue(result.isEmpty(),
                "injection payload must not return any rows — it should be a literal value");
    }

    @Test
    void findByName_stackedStatementPayload_isTreatedAsLiteral() {
        long before = countProducts();
        List<ProductOM> result = productDao.findByName("x'; DELETE FROM product; --");
        assertTrue(result.isEmpty());
        assertEquals(before, countProducts(), "no rows should have been deleted");
    }

    @Test
    void findByName_valueWithApostrophe_handledSafely() {
        long id = insertProductViaJdbc("O'Brien", 1.0, 1, "name with apostrophe");
        List<ProductOM> result = productDao.findByName("O'Brien");
        assertTrue(result.stream().anyMatch(p -> p.getId() == id));
    }

    private long countProducts() {
        try (var conn = getConnection(); var ps = conn.prepareStatement("SELECT COUNT(*) FROM product")) {
            var rs = ps.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
