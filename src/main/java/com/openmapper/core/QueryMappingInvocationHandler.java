package com.openmapper.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import com.openmapper.core.query.facade.QueryFacade;

public class QueryMappingInvocationHandler implements InvocationHandler {

    private final QueryFacade facade;
    private final DataSource dataSource;

    public QueryMappingInvocationHandler(QueryFacade facade, DataSource dataSource) {
        this.facade = facade;
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return facade.invokeMethodQueryWithParameters(method, args, dataSource);
    }

    protected DataSource getDataSource() {
        return dataSource;
    }
}
