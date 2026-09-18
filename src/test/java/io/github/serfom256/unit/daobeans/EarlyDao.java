package io.github.serfom256.unit.daobeans;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface EarlyDao {

    @DaoMethod(operation = DmlOperation.SELECT)
    String findAnything();
}
