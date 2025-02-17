package com.openmapper.core.query.model;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.util.Map;

import javax.sql.DataSource;

import com.openmapper.core.query.handlers.ResultSetHandler;
import com.openmapper.model.operations.DmlOperation;

public class QuerySpecifications {

    private DataSource dataSource;
    private Connection connection;
    private ResultSetHandler<?> handler;
    private Type returnType;
    private DmlOperation operation;
    private boolean shouldReturnGeneratedKeys;
    private Object previousOptimisticLockValue;
    private boolean hasOptimisticLock;
    private Map<String, Object> params;

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSetHandler<?> getHandler() {
        return handler;
    }

    public boolean shouldReturnGeneratedKeys() {
        return shouldReturnGeneratedKeys;
    }

    public Object getPreviousOptimisticLockValue() {
        return previousOptimisticLockValue;
    }

    public Type getReturnType() {
        return returnType;
    }

    public DmlOperation getOperation() {
        return operation;
    }

    public boolean hasOptimisticLock() {
        return hasOptimisticLock;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void setHandler(ResultSetHandler<?> handler) {
        this.handler = handler;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setOperation(DmlOperation operation) {
        this.operation = operation;
    }

    public void setShouldReturnGeneratedKeys(boolean shouldReturnGeneratedKeys) {
        this.shouldReturnGeneratedKeys = shouldReturnGeneratedKeys;
    }

    public void setPreviousOptimisticLockValue(Object previousOptimisticLockValue) {
        this.previousOptimisticLockValue = previousOptimisticLockValue;
    }

    public void setHasOptimisticLock(boolean hasOptimisticLock) {
        this.hasOptimisticLock = hasOptimisticLock;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
