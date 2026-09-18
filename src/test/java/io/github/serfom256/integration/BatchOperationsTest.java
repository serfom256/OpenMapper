package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.ProductOM;

class BatchOperationsTest extends IntegrationTestBase {

    private ProductOM product(String name) {
        ProductOM product = new ProductOM();
        product.setName(name);
        product.setPrice(1.0);
        product.setQuantity(1);
        product.setDescription("batch");
        return product;
    }

    private List<ProductOM> products(String name, int count) {
        List<ProductOM> products = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            products.add(product(name));
        }
        return products;
    }

    @Test
    @DisplayName("a collection parameter inserts every element and reports the count")
    void batchInsert() {
        String name = "batch-" + UUID.randomUUID();

        int affected = productDao.insertProducts(products(name, 50));

        assertEquals(50, affected);
        assertEquals(50, productDao.findByName(name).size());
        productDao.findByName(name).forEach(p -> productDao.deleteProduct(p.getId()));
    }

    @Test
    @DisplayName("an empty collection is a no-op rather than a failed statement")
    void emptyBatch() {
        assertEquals(0, productDao.insertProducts(List.of()));
    }

    @Test
    @DisplayName("a batch is one round trip, so it beats the same inserts one at a time")
    void batchIsFasterThanIndividualInserts() {
        String batched = "perf-batch-" + UUID.randomUUID();
        String individual = "perf-single-" + UUID.randomUUID();
        int count = 200;

        productDao.insertProducts(products(batched, 5));
        products(individual, 5).forEach(productDao::insertProduct);

        long batchNanos = System.nanoTime();
        productDao.insertProducts(products(batched, count));
        batchNanos = System.nanoTime() - batchNanos;

        long individualNanos = System.nanoTime();
        products(individual, count).forEach(productDao::insertProduct);
        individualNanos = System.nanoTime() - individualNanos;

        assertEquals(count + 5, productDao.findByName(batched).size());
        assertTrue(batchNanos < individualNanos,
                "batch took %dms, individual inserts took %dms".formatted(
                        batchNanos / 1_000_000, individualNanos / 1_000_000));

        productDao.findByName(batched).forEach(p -> productDao.deleteProduct(p.getId()));
        productDao.findByName(individual).forEach(p -> productDao.deleteProduct(p.getId()));
    }
}
