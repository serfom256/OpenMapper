package com.openmapper.core.facade;

import java.lang.reflect.Method;

public interface QueryFacade {
    Object invokeMethodQueryWithParameters(Method method, Object[] args);
}
