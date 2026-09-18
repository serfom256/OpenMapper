package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.InventoryOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface InventoryDao {

    @DaoMethod(operation = DmlOperation.MERGE)
    void upsertInventory(InventoryOM inventory);

    @DaoMethod(operation = DmlOperation.SELECT)
    InventoryOM findInventory(@Param(name = "sku") String sku);
}
