package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.UserOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface UserDao {

    @DaoMethod(operation = DmlOperation.SELECT)
    UserOM findUserWithProfile(@Param(name = "id") long id);
}
