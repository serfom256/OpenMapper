package io.github.serfom256.benchmark.dao;

import java.util.List;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.CatalogItemOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface CatalogItemDao {

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertCatalogItem(@Param(name = "item_type") String itemType,
                           @Param(name = "name") String name,
                           @Param(name = "bundle_size") Integer bundleSize,
                           @Param(name = "sku_code") String skuCode);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<CatalogItemOM> findCatalogItemsByName(@Param(name = "name") String name);

    @DaoMethod(operation = DmlOperation.INSERT, returnKeys = true)
    long insertJoinedItem(@Param(name = "item_type") String itemType, @Param(name = "name") String name);

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertJoinedBundle(@Param(name = "item_id") long itemId, @Param(name = "bundle_size") int bundleSize);

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertJoinedSku(@Param(name = "item_id") long itemId, @Param(name = "sku_code") String skuCode);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<CatalogItemOM> findJoinedItemsByName(@Param(name = "name") String name);
}
