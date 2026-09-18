package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.serfom256.benchmark.model.om.ProductOM;

/**
 * Database failures surface as Spring's {@code DataAccessException} hierarchy,
 * so callers handle them the same way they would with JPA or {@code JdbcTemplate}
 * rather than catching an openMapper-specific type.
 */
class ExceptionTranslationTest extends IntegrationTestBase {

    @Test
    @DisplayName("a NOT NULL violation becomes DataIntegrityViolationException")
    void notNullViolationIsTranslated() {
        ProductOM product = new ProductOM();
        product.setName(null);
        product.setPrice(1.0);
        product.setQuantity(1);
        product.setDescription("violates product.name NOT NULL");

        assertThrows(DataIntegrityViolationException.class, () -> productDao.insertProduct(product));
    }
}
