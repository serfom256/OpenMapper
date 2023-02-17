package com.openmapper.core.proxy;

import com.openmapper.core.annotations.DaoMethod;
import com.openmapper.core.annotations.SqlName;
import com.openmapper.core.impl.FsqlContext;
import com.openmapper.entity.FsqlEntity;
import com.openmapper.core.mapping.SqlBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class SqlInvocationHandler implements InvocationHandler {

    private final FsqlContext context;
    private final SqlBuilder sqlLoader;

    public SqlInvocationHandler(FsqlContext context) {
        this.context = context;
        this.sqlLoader = new SqlBuilder();
    }

    @Override
    public String invoke(Object proxy, Method method, Object[] args) {
        DaoMethod annotatedMethodName = method.getAnnotation(DaoMethod.class);
        if (annotatedMethodName == null) {
            throw new IllegalStateException(String.format("Method %s didn't annotated with @DaoMethod", method.getName()));
        }
        Parameter[] methodParams = method.getParameters();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter parameter = methodParams[i];
            Annotation[] annotArr = parameter.getAnnotations();
            for (Annotation annot : annotArr) {
                if (annot instanceof SqlName) {
                    params.put(((SqlName) annot).name(), args[i]);
                }
            }
        }

        FsqlEntity result = context.getSql(annotatedMethodName.sqlName());
        return sqlLoader.buildSql(result, params);
    }
}
