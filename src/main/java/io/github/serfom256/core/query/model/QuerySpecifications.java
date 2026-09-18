package io.github.serfom256.core.query.model;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.model.operations.DmlOperation;

public class QuerySpecifications {

    private DataSource dataSource;
    private ResultSetHandler<?> handler;
    private Type returnType;
    private DmlOperation operation;
    private boolean shouldReturnGeneratedKeys;
    private boolean hasOptimisticLock;
    private int fetchSize;
    private int queryTimeoutSeconds;
    private boolean streaming;
    private Map<String, Object> params;

    public QuerySpecifications copy() {
        QuerySpecifications copy = new QuerySpecifications();
        copy.dataSource = dataSource;
        copy.handler = handler;
        copy.returnType = returnType;
        copy.operation = operation;
        copy.shouldReturnGeneratedKeys = shouldReturnGeneratedKeys;
        copy.hasOptimisticLock = hasOptimisticLock;
        copy.fetchSize = fetchSize;
        copy.queryTimeoutSeconds = queryTimeoutSeconds;
        copy.streaming = streaming;
        copy.params = params == null ? null : new HashMap<>(params);
        return copy;
    }

    public DataSource getDataSource() {
        return dataSource;
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

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    public int getQueryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) {
        this.queryTimeoutSeconds = queryTimeoutSeconds;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public boolean hasOptimisticLock() {
        return hasOptimisticLock;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
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
