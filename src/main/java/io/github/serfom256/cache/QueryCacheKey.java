package io.github.serfom256.cache;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class QueryCacheKey {

    private final Method method;
    private final Object[] args;
    private final int hash;

    public QueryCacheKey(Method method, Object[] args) {
        this.method = method;
        this.args = args == null ? EMPTY : args.clone();
        this.hash = 31 * method.hashCode() + Arrays.hashCode(this.args);
    }

    private static final Object[] EMPTY = new Object[0];

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof QueryCacheKey)) {
            return false;
        }
        QueryCacheKey that = (QueryCacheKey) other;
        return method.equals(that.method) && Arrays.equals(args, that.args);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
