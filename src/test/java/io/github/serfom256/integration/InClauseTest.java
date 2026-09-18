package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.ProductOM;

class InClauseTest extends IntegrationTestBase {

    private long insert(String name) {
        return insertProductViaJdbc(name, 1.0, 1, "in-clause");
    }

    @Test
    @DisplayName("a collection parameter expands into one placeholder per element")
    void collectionExpandsToPlaceholders() {
        String prefix = "in-" + UUID.randomUUID();
        long first = insert(prefix + "-1");
        long second = insert(prefix + "-2");
        long excluded = insert(prefix + "-3");

        List<ProductOM> found = productDao.findProductsByIds(List.of(first, second));

        assertEquals(2, found.size());
        assertTrue(found.stream().noneMatch(p -> p.getId() == excluded));

        productDao.deleteProduct(first);
        productDao.deleteProduct(second);
        productDao.deleteProduct(excluded);
    }

    @Test
    @DisplayName("a single-element collection still works")
    void singleElement() {
        long id = insert("in-single-" + UUID.randomUUID());

        assertEquals(1, productDao.findProductsByIds(List.of(id)).size());

        productDao.deleteProduct(id);
    }

    @Test
    @DisplayName("an empty collection matches nothing instead of producing invalid SQL")
    void emptyCollectionMatchesNothing() {
        assertEquals(0, productDao.findProductsByIds(List.of()).size());
    }
}
