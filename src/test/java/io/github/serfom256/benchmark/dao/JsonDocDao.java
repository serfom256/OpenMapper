package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.JsonDocOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface JsonDocDao {

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertJsonDoc(JsonDocOM doc);

    @DaoMethod(operation = DmlOperation.SELECT)
    JsonDocOM findJsonDocByName(@Param(name = "name") String name);
}
