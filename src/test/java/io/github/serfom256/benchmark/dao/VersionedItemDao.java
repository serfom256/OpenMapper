package io.github.serfom256.benchmark.dao;

import java.util.List;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.VersionedItemOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface VersionedItemDao {

    @DaoMethod(operation = DmlOperation.SELECT)
    VersionedItemOM findVersionedItemById(@Param(name = "id") long id);

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertVersionedItem(VersionedItemOM item);

    @DaoMethod(operation = DmlOperation.UPDATE)
    void updateVersionedItem(VersionedItemOM item);

    @DaoMethod(procedure = "updateVersionedItem", operation = DmlOperation.UPDATE)
    int updateVersionedItems(List<VersionedItemOM> items);
}
