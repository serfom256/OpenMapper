package io.github.serfom256.integration;

import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.ProductOM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WriteOperationsTest extends IntegrationTestBase {

    @Test
    void insertProduct_persistsToDatabase() {
        ProductOM p = new ProductOM();
        p.setName("integration-insert");
        p.setPrice(55.55);
        p.setQuantity(7);
        p.setDescription("integration test insert");
        productDao.insertProduct(p);

        // Verify via JDBC that a row with this name exists
        try (var conn = getConnection();
             var ps = conn.prepareStatement("SELECT COUNT(*) FROM product WHERE name = ?")) {
            ps.setString(1, "integration-insert");
            var rs = ps.executeQuery();
            rs.next();
            assertTrue(rs.getInt(1) >= 1);
        } catch (Exception e) {
            fail("JDBC verification failed", e);
        }
    }

    @Test
    void updateProduct_changesFields() {
        long id = insertProductViaJdbc("update-target", 10.0, 1, "before");

        ProductOM p = new ProductOM();
        p.setId(id);
        p.setName("updated-name");
        p.setPrice(99.99);
        p.setQuantity(42);
        p.setDescription("after");
        productDao.updateProduct(p);

        ProductOM updated = productDao.findProductById(id);
        assertNotNull(updated);
        assertEquals("updated-name", updated.getName());
        assertEquals(99.99, updated.getPrice(), 0.01);
        assertEquals(42, updated.getQuantity());
        assertEquals("after", updated.getDescription());
    }

    @Test
    void deleteProduct_removesFromDatabase() {
        long id = insertProductViaJdbc("delete-target", 1.0, 1, "to-delete");
        assertTrue(existsInDb("product", id));

        productDao.deleteProduct(id);
        assertFalse(existsInDb("product", id));
    }

    @Test
    void deleteProduct_nonExistent_doesNotThrow() {
        assertDoesNotThrow(() -> productDao.deleteProduct(999_999L));
    }

    @Test
    void updateAuthor_changesFields() {
        long authorId = insertAuthorViaJdbc("original-author", "US");

        AuthorOM a = new AuthorOM();
        a.setId(authorId);
        a.setName("updated-author");
        a.setCountry("DE");
        authorDao.updateAuthor(a);

        try (var conn = getConnection();
             var ps = conn.prepareStatement("SELECT name, country FROM author WHERE id = ?")) {
            ps.setLong(1, authorId);
            var rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("updated-author", rs.getString("name"));
            assertEquals("DE", rs.getString("country"));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void deleteAuthor_cascadesToBooks() {
        long authorId = insertAuthorViaJdbc("cascade-author", "FR");
        insertBookViaJdbc("cascade-book-1", authorId, 2020, "CB-1");
        insertBookViaJdbc("cascade-book-2", authorId, 2021, "CB-2");

        assertTrue(existsInDb("author", authorId));
        authorDao.deleteAuthor(authorId);
        assertFalse(existsInDb("author", authorId));

        try (var conn = getConnection();
             var ps = conn.prepareStatement("SELECT COUNT(*) FROM book WHERE author_id = ?")) {
            ps.setLong(1, authorId);
            var rs = ps.executeQuery();
            rs.next();
            assertEquals(0, rs.getInt(1));
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void insertMultipleProducts_allPersist() {
        for (int i = 0; i < 5; i++) {
            ProductOM p = new ProductOM();
            p.setName("multi-insert-" + i);
            p.setPrice(i * 10.0);
            p.setQuantity(i);
            p.setDescription("multi " + i);
            productDao.insertProduct(p);
        }

        try (var conn = getConnection();
             var ps = conn.prepareStatement("SELECT COUNT(*) FROM product WHERE name LIKE 'multi-insert-%'")) {
            var rs = ps.executeQuery();
            rs.next();
            assertTrue(rs.getInt(1) >= 5);
        } catch (Exception e) {
            fail(e);
        }
    }
}
