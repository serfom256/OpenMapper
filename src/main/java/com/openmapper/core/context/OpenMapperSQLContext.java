package com.openmapper.core.context;

import com.openmapper.annotations.DaoMethod;
import com.openmapper.core.common.DaoMethodFacade;
import com.openmapper.exceptions.common.FunctionNotFoundException;
import com.openmapper.parser.model.SQLRecord;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenMapperSQLContext {

    private final Map<String, SQLRecord> context = new ConcurrentHashMap<>();

    private final DaoMethodFacade daoMethodFacade;

    public OpenMapperSQLContext(DaoMethodFacade daoMethodFacade) {
        this.daoMethodFacade = daoMethodFacade;
    }

    public void updateContext(String name, SQLRecord entity) {
        context.put(name, entity);
    }

    public SQLRecord getSqlProcedure(final Method method, Object[] arguments) {
        if (daoMethodFacade.hasEmbeddedSqlProcedure(method)) {
            return daoMethodFacade.extractProcedureFromMethodArguments(method, arguments);
        }
        return getSqlProcedure(getProcedureName(method));
    }

    private SQLRecord getSqlProcedure(final String name) {
        SQLRecord entity = context.get(name);
        if (entity == null) {
            throw new FunctionNotFoundException(name);
        }
        return entity;
    }

    private String getProcedureName(Method method) {
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        String procedure = method.getName();
        if (daoMethod != null && !daoMethod.procedure().isEmpty()) {
            procedure = daoMethod.procedure();
        }
        return procedure;
    }
}
