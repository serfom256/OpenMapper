package io.github.serfom256.core.query.method;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;

public class DaoMethodPlanRegistry {

    private final Map<Method, DaoMethodPlan> plans = new ConcurrentHashMap<>();
    private final DaoMethodPlanCompiler compiler;

    public DaoMethodPlanRegistry(ResultSetHandlerStrategy handlerStrategy) {
        this.compiler = new DaoMethodPlanCompiler(handlerStrategy);
    }

    public DaoMethodPlan planFor(Method method) {
        return plans.computeIfAbsent(method, compiler::compile);
    }
}
