package io.github.serfom256.exceptions.entity;

/**
 * Sentinel for {@link io.github.serfom256.annotations.DaoMethod#translateNotFoundExceptionTo()}
 * meaning "do not translate": a query that matches no row returns {@code null}
 * rather than throwing.
 *
 * <p>This is never instantiated or thrown. It exists because an annotation
 * attribute cannot default to {@code null}, so the "unset" state needs a type of
 * its own.
 */
public final class NoTranslationException extends RuntimeException {

    private NoTranslationException() {
        throw new AssertionError("NoTranslation is a marker and is never instantiated");
    }
}
