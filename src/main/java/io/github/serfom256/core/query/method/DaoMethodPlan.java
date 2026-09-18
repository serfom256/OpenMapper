package io.github.serfom256.core.query.method;

import java.lang.reflect.Type;

import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.model.operations.DmlOperation;

/**
 * Everything about a DAO method that is fixed for its lifetime: its procedure
 * name, DML operation, result handler and parameter bindings.
 *
 * <p>Resolved once per {@link java.lang.reflect.Method} and reused, so invoking
 * a DAO method performs no annotation lookups and allocates no reflection
 * metadata. This mirrors what {@link io.github.serfom256.core.query.mapping.MappingPlan}
 * already does for entity types.
 *
 * <p>Instances come from {@link #builder()}; the fields are a mix of three
 * booleans and two reference types that positional arguments made easy to
 * transpose silently.
 */
public final class DaoMethodPlan {

    private final String procedure;
    private final DmlOperation operation;
    private final boolean returnGeneratedKeys;
    private final Class<? extends Throwable> notFoundException;
    private final boolean cached;
    private final ResultSetHandler<?> handler;
    private final Type genericReturnType;
    private final boolean returnsOptional;
    private final boolean returnsStream;
    private final boolean returnsPage;
    private final String countProcedure;
    private final int sqlParameterIndex;
    private final ParameterBinding[] parameters;
    private final int batchParameterIndex;
    private final ParameterBinding batchElement;
    private final int fetchSize;
    private final int queryTimeoutSeconds;

    private DaoMethodPlan(Builder builder) {
        this.procedure = builder.procedure;
        this.operation = builder.operation;
        this.returnGeneratedKeys = builder.returnGeneratedKeys;
        this.notFoundException = builder.notFoundException;
        this.cached = builder.cached;
        this.handler = builder.handler;
        this.genericReturnType = builder.genericReturnType;
        this.returnsOptional = builder.returnsOptional;
        this.returnsStream = builder.returnsStream;
        this.returnsPage = builder.returnsPage;
        this.countProcedure = builder.countProcedure;
        this.sqlParameterIndex = builder.sqlParameterIndex;
        this.parameters = builder.parameters;
        this.batchParameterIndex = builder.batchParameterIndex;
        this.batchElement = builder.batchElement;
        this.fetchSize = builder.fetchSize;
        this.queryTimeoutSeconds = builder.queryTimeoutSeconds;
    }

    static Builder builder() {
        return new Builder();
    }

    public String procedure() {
        return procedure;
    }

    public DmlOperation operation() {
        return operation;
    }

    public boolean returnGeneratedKeys() {
        return returnGeneratedKeys;
    }

    /** The exception to raise when nothing is found, or {@code null} to return null. */
    public Class<? extends Throwable> notFoundException() {
        return notFoundException;
    }

    public boolean isCached() {
        return cached;
    }

    public ResultSetHandler<?> handler() {
        return handler;
    }

    public Type genericReturnType() {
        return genericReturnType;
    }

    public boolean returnsOptional() {
        return returnsOptional;
    }

    public boolean returnsStream() {
        return returnsStream;
    }

    public boolean returnsPage() {
        return returnsPage;
    }

    public String countProcedure() {
        return countProcedure;
    }

    /** Index of the {@code @Sql} parameter carrying the statement, or -1 if absent. */
    public int sqlParameterIndex() {
        return sqlParameterIndex;
    }

    public boolean hasEmbeddedSql() {
        return sqlParameterIndex >= 0;
    }

    public ParameterBinding[] parameters() {
        return parameters;
    }

    public boolean isBatch() {
        return batchParameterIndex >= 0;
    }

    public int batchParameterIndex() {
        return batchParameterIndex;
    }

    public ParameterBinding batchElement() {
        return batchElement;
    }

    public int fetchSize() {
        return fetchSize;
    }

    public int queryTimeoutSeconds() {
        return queryTimeoutSeconds;
    }

    static final class Builder {

        private String procedure;
        private DmlOperation operation;
        private boolean returnGeneratedKeys;
        private Class<? extends Throwable> notFoundException;
        private boolean cached;
        private ResultSetHandler<?> handler;
        private Type genericReturnType;
        private boolean returnsOptional;
        private boolean returnsStream;
        private boolean returnsPage;
        private String countProcedure;
        private int sqlParameterIndex = -1;
        private ParameterBinding[] parameters = new ParameterBinding[0];
        private int batchParameterIndex = -1;
        private ParameterBinding batchElement;
        private int fetchSize;
        private int queryTimeoutSeconds;

        private Builder() {
        }

        Builder procedure(String procedure) {
            this.procedure = procedure;
            return this;
        }

        Builder operation(DmlOperation operation) {
            this.operation = operation;
            return this;
        }

        Builder returnGeneratedKeys(boolean returnGeneratedKeys) {
            this.returnGeneratedKeys = returnGeneratedKeys;
            return this;
        }

        Builder notFoundException(Class<? extends Throwable> notFoundException) {
            this.notFoundException = notFoundException;
            return this;
        }

        Builder cached(boolean cached) {
            this.cached = cached;
            return this;
        }

        Builder handler(ResultSetHandler<?> handler) {
            this.handler = handler;
            return this;
        }

        Builder genericReturnType(Type genericReturnType) {
            this.genericReturnType = genericReturnType;
            return this;
        }

        Builder returnsOptional(boolean returnsOptional) {
            this.returnsOptional = returnsOptional;
            return this;
        }

        Builder returnsStream(boolean returnsStream) {
            this.returnsStream = returnsStream;
            return this;
        }

        Builder returnsPage(boolean returnsPage) {
            this.returnsPage = returnsPage;
            return this;
        }

        Builder countProcedure(String countProcedure) {
            this.countProcedure = countProcedure;
            return this;
        }

        Builder sqlParameterIndex(int sqlParameterIndex) {
            this.sqlParameterIndex = sqlParameterIndex;
            return this;
        }

        Builder parameters(ParameterBinding[] parameters) {
            this.parameters = parameters;
            return this;
        }

        Builder batchParameterIndex(int batchParameterIndex) {
            this.batchParameterIndex = batchParameterIndex;
            return this;
        }

        Builder batchElement(ParameterBinding batchElement) {
            this.batchElement = batchElement;
            return this;
        }

        Builder fetchSize(int fetchSize) {
            this.fetchSize = fetchSize;
            return this;
        }

        Builder queryTimeoutSeconds(int queryTimeoutSeconds) {
            this.queryTimeoutSeconds = queryTimeoutSeconds;
            return this;
        }

        DaoMethodPlan build() {
            return new DaoMethodPlan(this);
        }
    }
}
