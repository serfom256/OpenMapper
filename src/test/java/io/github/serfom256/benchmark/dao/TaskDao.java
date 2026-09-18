package io.github.serfom256.benchmark.dao;

import java.util.List;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.TaskOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface TaskDao {

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertTask(TaskOM task);

    @DaoMethod(operation = DmlOperation.SELECT)
    TaskOM findTaskById(@Param(name = "id") long id);

    @DaoMethod(operation = DmlOperation.SELECT)
    List<TaskOM> findTasksByStatus(@Param(name = "status") int status);
}
