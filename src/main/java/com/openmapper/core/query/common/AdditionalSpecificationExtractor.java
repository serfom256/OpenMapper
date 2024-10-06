package com.openmapper.core.query.common;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.openmapper.annotations.DaoMethod;
import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.handlers.ResultSetHandlerStrategy;
import com.openmapper.core.query.handlers.ResultSetHandler;
import com.openmapper.core.query.model.QuerySpecifications;

@Component
public class AdditionalSpecificationExtractor {

    private final ResultSetHandlerStrategy resultHandlerStrategy;

    public AdditionalSpecificationExtractor(ResultSetHandlerStrategy resultHandlerStrategy) {
        this.resultHandlerStrategy = resultHandlerStrategy;
    }

    public QuerySpecifications extractAdditionalQuerySpecifications(Method method, QuerySpecifications querySpecifications) {
        Class<?> returnType = getReturnType(method);
        Type genericReturnType = getGenericReturnType(method);
        DmlOperation operationType = getOperationType(method);
        ResultSetHandler<?> handlerByMethodReturnType = resultHandlerStrategy.getHandlerByMethodReturnType(returnType);
        boolean returnGeneratedKeys = returnGeneratedKeys(method);

        querySpecifications.setHandler(handlerByMethodReturnType);
        querySpecifications.setOperation(operationType);
        querySpecifications.setReturnType(genericReturnType);
        querySpecifications.setShouldReturnGeneratedKeys(returnGeneratedKeys);
        return querySpecifications;
    }

    private Class<?> getReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) {
            return ((Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0]);
        }
        return method.getReturnType();
    }

    protected Type getGenericReturnType(Method method) {
        Type genericReturnType = method.getGenericReturnType();
        if (method.getReturnType() == Optional.class) {
            return ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
        }
        return genericReturnType;
    }

    private DmlOperation getOperationType(Method method) {
        DmlOperation operation = DmlOperation.SELECT;
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        if (daoMethod != null) {
            operation = daoMethod.operation();
        }
        return operation;
    }

    private boolean returnGeneratedKeys(Method method) {
        DaoMethod daoMethod = method.getAnnotation(DaoMethod.class);
        return daoMethod != null && daoMethod.returnKeys();
    }
}
