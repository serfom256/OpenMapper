package com.openmapper.core.query;

import java.lang.reflect.Type;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.handlers.ResultSetHandler;

public class MethodSpecifications {

    private final ResultSetHandler<?> handler;
    private final Type returnType;
    private final DmlOperation operation;
    private final boolean shouldReturnGeneratedKeys;

    public MethodSpecifications(
            ResultSetHandler<?> handler,
            Type returnType,
            DmlOperation operation,
            boolean shouldReturnGeneratedKeys) {
        this.handler = handler;
        this.returnType = returnType;
        this.operation = operation;
        this.shouldReturnGeneratedKeys = shouldReturnGeneratedKeys;
    }

    public ResultSetHandler<?> getHandler() {
        return handler;
    }

    public boolean shouldReturnGeneratedKeys() {
        return shouldReturnGeneratedKeys;
    }

    public Type getReturnType() {
        return returnType;
    }

    public DmlOperation getOperation() {
        return operation;
    }
}
