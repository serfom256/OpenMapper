package io.github.serfom256.core.query.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


/**
 * Raises the exception a DAO method asked for when its query matched no row.
 *
 * <p>Whether to translate at all, and to what, is decided once when the method's
 * {@link io.github.serfom256.core.query.method.DaoMethodPlan} is built -- including
 * skipping return types that already express absence. This class only acts on
 * that decision, so nothing here runs on the hot path unless a query really did
 * come back empty.
 */
public class NotFoundTranslator {

    private static final String NOT_FOUND_MESSAGE = "No result found for %s.%s()";
    private static final String UNCONSTRUCTIBLE_MESSAGE =
            "%s cannot be used for translateNotFoundExceptionTo: it needs a public (String)"
                    + " or no-argument constructor";
    private static final String INSTANTIATION_FAILED_MESSAGE =
            "Could not instantiate %s for translateNotFoundExceptionTo on %s";
    private static final String UNDECLARED_CHECKED_MESSAGE =
            "%s is a checked exception, so %s.%s() must declare it in a throws clause"
                    + " (or use an unchecked exception instead)";

    /**
     * Throws the configured exception when {@code result} is absent and the
     * method opted into translation; otherwise returns normally.
     */
    public void translateIfAbsent(Class<? extends Throwable> notFoundException, Method method, Object result) {
        if (result != null || notFoundException == null) {
            return;
        }
        rethrow(instantiate(notFoundException, method), notFoundException, method);
    }

    private Throwable instantiate(Class<? extends Throwable> type, Method method) {
        String message = NOT_FOUND_MESSAGE.formatted(
                method.getDeclaringClass().getSimpleName(), method.getName());
        Constructor<? extends Throwable> withMessage = constructor(type, String.class);
        try {
            if (withMessage != null) {
                return withMessage.newInstance(message);
            }
            Constructor<? extends Throwable> noArg = constructor(type);
            if (noArg == null) {
                throw new IllegalStateException(UNCONSTRUCTIBLE_MESSAGE.formatted(type.getName()));
            }
            return noArg.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    INSTANTIATION_FAILED_MESSAGE.formatted(type.getName(), method), e);
        }
    }

    private Constructor<? extends Throwable> constructor(Class<? extends Throwable> type,
                                                         Class<?>... parameterTypes) {
        try {
            return type.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void rethrow(Throwable translated, Class<? extends Throwable> target, Method method) {
        if (translated instanceof RuntimeException || translated instanceof Error) {
            sneakyThrow(translated);
        }
        if (!declares(method, target)) {
            throw new IllegalStateException(UNDECLARED_CHECKED_MESSAGE.formatted(
                    target.getName(), method.getDeclaringClass().getSimpleName(), method.getName()));
        }
        sneakyThrow(translated);
    }

    private boolean declares(Method method, Class<? extends Throwable> target) {
        for (Class<?> declared : method.getExceptionTypes()) {
            if (declared.isAssignableFrom(target)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
