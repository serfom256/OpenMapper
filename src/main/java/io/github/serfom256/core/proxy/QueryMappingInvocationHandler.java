package io.github.serfom256.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import io.github.serfom256.core.query.facade.QueryFacade;

public class QueryMappingInvocationHandler implements InvocationHandler {

    private final QueryFacade facade;
    private final DataSource dataSource;

    public QueryMappingInvocationHandler(QueryFacade facade, DataSource dataSource) {
        this.facade = facade;
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return invokeObjectMethod(proxy, method, args);
        }
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        return facade.invokeMethodQueryWithParameters(method, args, dataSource);
    }

    private Object invokeObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "equals" -> proxy == args[0];
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> "%s[openMapper proxy]".formatted(proxy.getClass().getInterfaces()[0].getName());
            default -> throw new UnsupportedOperationException(method.toString());
        };
    }

    protected DataSource getDataSource() {
        return dataSource;
    }
}
