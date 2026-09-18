package io.github.serfom256.core.query.method;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;

/**
 * Lazily builds and caches one {@link DaoMethodPlan} per DAO method.
 *
 * <p>Keyed by the {@link Method} itself. An earlier version of this lookup hashed
 * the method and its parameters into an {@code int} and used that as identity,
 * which let two distinct methods collide onto one entry.
 */
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
