package com.openmapper.core.context;

import com.openmapper.annotations.DaoMethod;
import com.openmapper.exceptions.common.FunctionNotFoundException;
import com.openmapper.parser.model.SQLRecord;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenMapperSQLContext {

    private final Map<String, SQLRecord> context = new ConcurrentHashMap<>(32);

    public void updateContext(String name, SQLRecord entity) {
        context.put(name, entity);
    }

    public SQLRecord getSqlProcedure(final String name) {
        SQLRecord entity = context.get(name);
        if (entity == null) {
            throw new FunctionNotFoundException(name);
        }
        return entity;
    }

    public SQLRecord getSqlProcedure(final Method method) {
        return getSqlProcedure(getProcedureName(method));
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
