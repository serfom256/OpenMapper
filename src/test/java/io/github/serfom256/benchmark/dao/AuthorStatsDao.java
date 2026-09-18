package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.AuthorStatsOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface AuthorStatsDao {

    @DaoMethod(procedure = "findAuthorWithBookCount", operation = DmlOperation.SELECT)
    AuthorStatsOM findWithBookCount(@Param(name = "id") long id);

    @DaoMethod(procedure = "insertAuthorComputed", operation = DmlOperation.INSERT, returnKeys = true)
    long insert(AuthorStatsOM author);
}
