package com.openmapper.core.proxy;

import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.Param;
import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.files.mapping.InputMapper;
import com.openmapper.core.files.mapping.InputMapperImpl;
import com.openmapper.core.entity.FsqlEntity;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.SQL_TRACING;

public class SqlExtractor implements InvocationHandler {

    private final OpenMapperSqlContext context;
    private final InputMapper mapper;

    public SqlExtractor(OpenMapperSqlContext context, Environment environment) {
        this.context = context;
        final String property = environment.getProperty(SQL_TRACING.getValue());
        this.mapper = new InputMapperImpl(Boolean.parseBoolean(property == null ? "false" : property));
    }

    @Override
    public String invoke(Object proxy, Method method, Object[] args) {
        DaoMethod annotatedMethodName = method.getAnnotation(DaoMethod.class);
        if (annotatedMethodName == null) {
            throw new IllegalStateException(String.format("Method %s doesn't annotated with @DaoMethod", method.getName()));
        }
        FsqlEntity result = context.getSql(annotatedMethodName.procedure());
        return mapper.mapSql(result, extractMethodParams(method, args));
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
