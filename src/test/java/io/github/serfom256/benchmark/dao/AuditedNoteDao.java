package io.github.serfom256.benchmark.dao;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.AuditedNoteOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface AuditedNoteDao {

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertAuditedNote(AuditedNoteOM note);

    @DaoMethod(operation = DmlOperation.SELECT)
    AuditedNoteOM findAuditedNoteById(@Param(name = "id") long id);
}
