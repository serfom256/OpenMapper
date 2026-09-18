package io.github.serfom256.benchmark.dao;

import java.util.List;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.benchmark.model.om.TicketOM;
import io.github.serfom256.model.operations.DmlOperation;

@DaoLayer
public interface TicketDao {

    @DaoMethod(operation = DmlOperation.INSERT)
    void insertTicket(TicketOM ticket);

    @DaoMethod(procedure = "insertTicket", operation = DmlOperation.INSERT)
    int insertTickets(List<TicketOM> tickets);

    @DaoMethod(operation = DmlOperation.SELECT)
    TicketOM findTicketById(@Param(name = "id") String id);
}
