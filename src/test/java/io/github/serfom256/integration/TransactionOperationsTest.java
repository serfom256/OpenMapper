package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.serfom256.benchmark.model.om.ProductOM;

class TransactionOperationsTest extends IntegrationTestBase {

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactions() {
        return new TransactionTemplate(transactionManager);
    }

    private ProductOM newProduct(String name) {
        ProductOM product = new ProductOM();
        product.setName(name);
        product.setPrice(9.99);
        product.setQuantity(1);
        product.setDescription("tx test");
        return product;
    }

    private long idOf(String name) {
        List<ProductOM> found = productDao.findByName(name);
        assertEquals(1, found.size(), "expected exactly one product named " + name);
        return found.get(0).getId();
    }

    @Test
    @DisplayName("a rollback undoes an openMapper insert")
    void rollback_undoesInsert() {
        String name = "tx-rollback-" + UUID.randomUUID();

        assertThrows(IllegalStateException.class, () -> transactions().execute(status -> {
            productDao.insertProduct(newProduct(name));
            throw new IllegalStateException("forced rollback");
        }));

        assertTrue(productDao.findByName(name).isEmpty(),
                "insert survived a rolled back transaction: the DAO did not join it");
    }

    @Test
    @DisplayName("a commit persists an openMapper insert")
    void commit_persistsInsert() {
        String name = "tx-commit-" + UUID.randomUUID();

        transactions().execute(status -> {
            productDao.insertProduct(newProduct(name));
            return null;
        });

        long id = idOf(name);
        assertTrue(existsInDb("product", id));
        productDao.deleteProduct(id);
    }

    @Test
    @DisplayName("a read inside the transaction sees the transaction's own uncommitted write")
    void readSeesUncommittedWriteOfSameTransaction() {
        String name = "tx-readback-" + UUID.randomUUID();

        Long idInsideTransaction = transactions().execute(status -> {
            productDao.insertProduct(newProduct(name));
            List<ProductOM> visible = productDao.findByName(name);
            assertEquals(1, visible.size(),
                    "the DAO read on a different connection than it wrote on");
            long id = visible.get(0).getId();
            assertNotNull(productDao.findProductById(id));
            status.setRollbackOnly();
            return id;
        });

        assertNotNull(idInsideTransaction);
        assertFalse(existsInDb("product", idInsideTransaction),
                "rollback did not remove the row");
    }

    @Test
    @DisplayName("a rollback undoes an openMapper update")
    void rollback_undoesUpdate() {
        long id = insertProductViaJdbc("tx-update-" + UUID.randomUUID(), 1.0, 1, "before");

        assertThrows(IllegalStateException.class, () -> transactions().execute(status -> {
            ProductOM product = productDao.findProductById(id);
            product.setDescription("after");
            productDao.updateProduct(product);
            throw new IllegalStateException("forced rollback");
        }));

        assertEquals("before", productDao.findProductById(id).getDescription(),
                "update survived a rolled back transaction");
        productDao.deleteProduct(id);
    }
}
