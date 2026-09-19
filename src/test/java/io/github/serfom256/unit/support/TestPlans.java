package io.github.serfom256.unit.support;

import java.lang.reflect.Method;

import io.github.serfom256.core.query.handlers.EmptyResultSetHandler;
import io.github.serfom256.core.query.handlers.EntityResultSetHandler;
import io.github.serfom256.core.query.handlers.IterableResultSetHandler;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.core.query.method.DaoMethodPlanRegistry;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;

public final class TestPlans {

    private static final DaoMethodPlanRegistry REGISTRY = new DaoMethodPlanRegistry(handlerStrategy());

    private TestPlans() {
    }

    public static DaoMethodPlan planFor(Method method) {
        return REGISTRY.planFor(method);
    }

    public static DaoMethodPlan planFor(Class<?> dao, String methodName, Class<?>... parameterTypes) {
        try {
            return planFor(dao.getDeclaredMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static ResultSetHandlerStrategy strategy() {
        return handlerStrategy();
    }

    private static ResultSetHandlerStrategy handlerStrategy() {
        ResultSetGraphReader graphReader = new ResultSetGraphReader(new MappingPlanRegistry(null));
        ResultSetObjectMapper objectMapper = new ResultSetObjectMapper(graphReader);
        ResultSetPrimitiveMapper primitiveMapper = new ResultSetPrimitiveMapper();
        return new ResultSetHandlerStrategy(
                new EntityResultSetHandler(objectMapper, primitiveMapper),
                new EmptyResultSetHandler(),
                new IterableResultSetHandler(objectMapper, primitiveMapper));
    }
}
