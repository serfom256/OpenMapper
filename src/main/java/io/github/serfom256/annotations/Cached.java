package io.github.serfom256.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Caches the prepared query for this method, keyed by the method and its
 * argument values.
 *
 * <p>What is cached is the <em>bound query</em> -- the parsed SQL and its
 * parameter binding -- not the result set. The statement still executes on every
 * call; the saving is the parse and bind work.
 *
 * <p>This opts a single method in regardless of the global
 * {@code openmapper.query.cache.enabled} setting, so it is the way to cache
 * selectively while leaving the default off.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Cached {
}
