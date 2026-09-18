package io.github.serfom256.core.context;

import io.github.serfom256.core.common.DaoMethodFacade;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.exceptions.common.FunctionNotFoundException;
import io.github.serfom256.parser.model.SQLRecord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OpenMapperSQLContext {

    private final Map<String, SQLRecord> context = new ConcurrentHashMap<>();

    private final DaoMethodFacade daoMethodFacade;

    public OpenMapperSQLContext(DaoMethodFacade daoMethodFacade) {
        this.daoMethodFacade = daoMethodFacade;
    }

    public void updateContext(String name, SQLRecord entity) {
        context.put(name, entity);
    }

    public SQLRecord getSqlProcedure(final DaoMethodPlan plan, Object[] arguments) {
        if (plan.hasEmbeddedSql()) {
            return daoMethodFacade.extractProcedureFromMethodArguments(plan, arguments);
        }
        return getSqlProcedure(plan.procedure());
    }

    public SQLRecord getSqlProcedure(final String name) {
        SQLRecord entity = context.get(name);
        if (entity == null) {
            throw new FunctionNotFoundException(name);
        }
        return entity;
    }
}
