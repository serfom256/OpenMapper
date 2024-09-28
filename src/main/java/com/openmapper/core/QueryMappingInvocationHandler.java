package com.openmapper.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.openmapper.core.query.facade.QueryFacade;

public class QueryMappingInvocationHandler implements InvocationHandler {

    private final QueryFacade facade; 

    public QueryMappingInvocationHandler(QueryFacade facade) {
        this.facade = facade;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        return facade.invokeMethodQueryWithParameters(method, args);
    }
}
