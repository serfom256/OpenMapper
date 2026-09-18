package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.InventoryOM;
import io.github.serfom256.exceptions.internal.OptimisticLockException;

class UpsertTest extends IntegrationTestBase {

    private InventoryOM item(String sku, int quantity, long version) {
        InventoryOM inventory = new InventoryOM();
        inventory.setSku(sku);
        inventory.setQuantity(quantity);
        inventory.setVersion(version);
        return inventory;
    }

    @Test
    @DisplayName("an upsert inserts when the row is absent")
    void upsertInserts() {
        String sku = "sku-" + UUID.randomUUID();
        assertNull(inventoryDao.findInventory(sku));

        inventoryDao.upsertInventory(item(sku, 10, 0));

        InventoryOM stored = inventoryDao.findInventory(sku);
        assertEquals(10, stored.getQuantity());
        assertEquals(1, stored.getVersion());
    }

    @Test
    @DisplayName("an upsert updates when the row exists, advancing the version")
    void upsertUpdates() {
        String sku = "sku-" + UUID.randomUUID();
        inventoryDao.upsertInventory(item(sku, 10, 0));
        InventoryOM stored = inventoryDao.findInventory(sku);

        inventoryDao.upsertInventory(item(sku, 25, stored.getVersion()));

        InventoryOM updated = inventoryDao.findInventory(sku);
        assertEquals(25, updated.getQuantity());
        assertEquals(2, updated.getVersion());
    }

    @Test
    @DisplayName("an upsert against a stale version is rejected like an update")
    void upsertRespectsOptimisticLock() {
        String sku = "sku-" + UUID.randomUUID();
        inventoryDao.upsertInventory(item(sku, 10, 0));
        inventoryDao.upsertInventory(item(sku, 20, 1));

        assertThrows(OptimisticLockException.class,
                () -> inventoryDao.upsertInventory(item(sku, 30, 1)));

        assertEquals(20, inventoryDao.findInventory(sku).getQuantity());
    }
}
