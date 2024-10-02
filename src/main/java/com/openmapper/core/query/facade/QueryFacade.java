package com.openmapper.core.query.facade;

import java.lang.reflect.Method;

import javax.sql.DataSource;

public interface QueryFacade {
    Object invokeMethodQueryWithParameters(Method method, Object[] args, DataSource dataSource);
}
