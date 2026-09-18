package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.StudentOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface StudentDao {

    @DaoMethod(operation = DmlOperation.SELECT)
    StudentOM findStudentWithCourses(@Param(name = "id") long id);

    @DaoMethod(procedure = "insertStudent", operation = DmlOperation.INSERT, returnKeys = true)
    long insertStudent(StudentOM student);
}
