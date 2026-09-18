package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a mapped entity: a type openMapper can populate from a result
 * set and flatten back into bind parameters.
 *
 * <p>Only fields carrying a mapping annotation participate -- {@link Field},
 * {@link Joined}, {@link Nested} or {@link OptimisticLockField}. Everything else
 * is ignored, so transient state needs no opt-out.
 *
 * <p>Models are discovered through {@code openmapper.model.packagesToScan}.
 *
 * @see Dto for parameter-only types that need no identity
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface Model {

    /**
     * Name of the <em>Java field</em> -- not the column -- that identifies an
     * instance. Row-to-object assembly uses it to tell rows of the same entity
     * apart when a join produces one row per associated child, so it must be
     * set for {@link Joined} associations to collapse correctly.
     *
     * @return the identifying field's name
     */
    String primaryKey();
}
