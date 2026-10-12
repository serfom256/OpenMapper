package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.NullableBoxOM;
import io.github.serfom256.benchmark.model.om.PrimitiveBoxOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface NullableBoxDao {

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertNullableBox(NullableBoxOM box);

    @DaoMethod(operation = DmlOperation.SELECT)
    NullableBoxOM findNullableBoxByName(@Param(name = "name") String name);

    @DaoMethod(operation = DmlOperation.SELECT)
    PrimitiveBoxOM findPrimitiveBoxByName(@Param(name = "name") String name);
    @DaoMethod(operation = DmlOperation.SELECT)
    Integer findMaxIntValueByName(@Param(name = "name") String name);
}
