package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.BundleItemOM;
import io.github.serfom256.benchmark.model.om.CatalogItemOM;
import io.github.serfom256.benchmark.model.om.SkuItemOM;
import io.github.serfom256.exceptions.entity.ModelValidationException;

class PolymorphicMappingTest extends IntegrationTestBase {

    @Test
    @DisplayName("the discriminator column picks the concrete type per row")
    void discriminatorSelectsSubtype() {
        String name = "catalog-" + UUID.randomUUID();
        catalogItemDao.insertCatalogItem("BUNDLE", name, 4, null);
        catalogItemDao.insertCatalogItem("SKU", name, null, "SKU-9");

        List<CatalogItemOM> items = catalogItemDao.findCatalogItemsByName(name);

        assertEquals(2, items.size());
        BundleItemOM bundle = assertInstanceOf(BundleItemOM.class, items.get(0));
        SkuItemOM sku = assertInstanceOf(SkuItemOM.class, items.get(1));
        assertEquals(4, bundle.getBundleSize());
        assertEquals("SKU-9", sku.getSkuCode());
        assertEquals(name, bundle.getName());
        assertTrue(bundle.getId() > 0);
    }

    @Test
    @DisplayName("the same models map a base table outer-joined to per-subtype tables")
    void discriminatorSelectsSubtypeAcrossJoinedTables() {
        String name = "joined-" + UUID.randomUUID();
        long bundleId = catalogItemDao.insertJoinedItem("BUNDLE", name);
        catalogItemDao.insertJoinedBundle(bundleId, 7);
        long skuId = catalogItemDao.insertJoinedItem("SKU", name);
        catalogItemDao.insertJoinedSku(skuId, "SKU-J");

        List<CatalogItemOM> items = catalogItemDao.findJoinedItemsByName(name);

        assertEquals(2, items.size());
        assertEquals(7, assertInstanceOf(BundleItemOM.class, items.get(0)).getBundleSize());
        assertEquals("SKU-J", assertInstanceOf(SkuItemOM.class, items.get(1)).getSkuCode());
    }

    @Test
    @DisplayName("a NULL discriminator maps to the base type")
    void nullDiscriminatorMapsToBaseType() {
        String name = "catalog-null-" + UUID.randomUUID();
        catalogItemDao.insertCatalogItem(null, name, null, null);

        List<CatalogItemOM> items = catalogItemDao.findCatalogItemsByName(name);

        assertEquals(1, items.size());
        assertEquals(CatalogItemOM.class, items.get(0).getClass());
    }

    @Test
    @DisplayName("an undeclared discriminator value fails, naming the column and value")
    void unknownDiscriminatorFails() {
        String name = "catalog-unknown-" + UUID.randomUUID();
        catalogItemDao.insertCatalogItem("GIFT_CARD", name, null, null);

        ModelValidationException failure = assertThrows(ModelValidationException.class,
                () -> catalogItemDao.findCatalogItemsByName(name));

        assertTrue(failure.getMessage().contains("item_type"), failure.getMessage());
        assertTrue(failure.getMessage().contains("GIFT_CARD"), failure.getMessage());
    }
}
