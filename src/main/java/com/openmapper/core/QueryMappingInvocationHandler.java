package com.openmapper.core;

import com.openmapper.core.facade.QueryFacade;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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
