package com.openmapper.core.proxy;

import com.openmapper.util.SqlMapperImpl;
import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.Param;
import com.openmapper.core.impl.FsqlContext;
import com.openmapper.core.strategy.JdbcQueryExecutor;
import com.openmapper.core.query.QueryExecutor;
import com.openmapper.core.query.QueryExecutorStrategy;
import com.openmapper.entity.FsqlEntity;
import com.openmapper.util.SqlMapper;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.SQL_TRACING;

public class EntityMappingInvocationHandler implements InvocationHandler {

    private final FsqlContext context;
    private final SqlMapper mapper;
    private final QueryExecutorStrategy strategy = new QueryExecutorStrategy();
    private final QueryExecutor queryExecutor;


    public EntityMappingInvocationHandler(FsqlContext context, Environment environment, DataSource dataSource) {
        this.context = context;
        this.queryExecutor = new JdbcQueryExecutor(dataSource);
        final String property = environment.getProperty(SQL_TRACING.getValue());
        this.mapper = new SqlMapperImpl(Boolean.parseBoolean(property == null ? "false" : property)); // fixme
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        DaoMethod annotatedMethodName = method.getAnnotation(DaoMethod.class);
        if (annotatedMethodName == null) {
            throw new IllegalStateException(String.format("Method %s doesn't annotated with @DaoMethod", method.getName()));
        }
        FsqlEntity result = context.getSql(annotatedMethodName.procedure());
        final String query = mapper.mapSql(result, extractMethodParams(method, args));
        try {
            return queryExecutor.execute(query, strategy.getExecutorByMethodReturnType(method.getReturnType()), method.getGenericReturnType());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> extractMethodParams(Method method, Object[] args) {
        Parameter[] methodParams = method.getParameters();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter parameter = methodParams[i];
            Annotation[] annotArr = parameter.getAnnotations();
            for (Annotation annot : annotArr) {
                if (annot instanceof Param) {
                    params.put(((Param) annot).name(), args[i]);
                }
            }
        }
        return params;
    }
}
