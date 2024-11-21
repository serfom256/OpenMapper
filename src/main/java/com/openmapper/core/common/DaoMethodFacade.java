package com.openmapper.core.common;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.openmapper.annotations.entity.Sql;
import com.openmapper.core.context.DaoMethodContext;
import com.openmapper.parser.SourceMapper;
import com.openmapper.parser.model.SQLRecord;

@Component
public class DaoMethodFacade {

    private final DaoMethodContext daoMethodContext;
    private final SourceMapper mapper;

    public DaoMethodFacade(DaoMethodContext daoMethodContext, SourceMapper mapper) {
        this.daoMethodContext = daoMethodContext;
        this.mapper = mapper;
    }

    public boolean hasEmbeddedSqlProcedure(Method method){
        return daoMethodContext.hasEmbeddedSqlProcedure(method);
    }

    public SQLRecord extractProcedureFromMethodArguments(Method method, Object[] arguments) {
        final String sqlProcedure = extractSqlProcedureFromMethodArguments(method, arguments);
        SQLRecord cachedRecord = daoMethodContext.getSqlRecordByProcedure(sqlProcedure);
        if(cachedRecord == null){
            cachedRecord = mapper.map(sqlProcedure);
            daoMethodContext.registerSqlProcedure(sqlProcedure, cachedRecord);
        }
        return cachedRecord;
    }

    @NonNull
    private String extractSqlProcedureFromMethodArguments(Method method, Object[] arguments) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < arguments.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getAnnotation(Sql.class) != null) {
                return (String) arguments[i];
            }
        }

        throw new IllegalStateException("No parameter with @Sql annotation found!");
    }
}
