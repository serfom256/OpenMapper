package io.github.serfom256.core.common;

import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLRecord;

public class DaoMethodFacade {

    private static final String NO_SQL_PARAMETER = "No parameter with @Sql annotation found!";

    private final DaoMethodContext daoMethodContext;
    private final SourceMapper mapper;

    public DaoMethodFacade(DaoMethodContext daoMethodContext, SourceMapper mapper) {
        this.daoMethodContext = daoMethodContext;
        this.mapper = mapper;
    }

    public SQLRecord extractProcedureFromMethodArguments(DaoMethodPlan plan, Object[] arguments) {
        final String sqlProcedure = sqlFrom(plan, arguments);
        SQLRecord cachedRecord = daoMethodContext.getSqlRecordByProcedure(sqlProcedure);
        if (cachedRecord == null) {
            cachedRecord = mapper.map(sqlProcedure);
            daoMethodContext.registerSqlProcedure(sqlProcedure, cachedRecord);
        }
        return cachedRecord;
    }

    private String sqlFrom(DaoMethodPlan plan, Object[] arguments) {
        int index = plan.sqlParameterIndex();
        if (index < 0 || arguments == null || index >= arguments.length) {
            throw new IllegalStateException(NO_SQL_PARAMETER);
        }
        return (String) arguments[index];
    }
}
