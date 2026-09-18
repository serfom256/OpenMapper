package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a parameter object: its {@link Field}-annotated fields are
 * flattened into bind parameters when it is passed to a DAO method, so a group
 * of related arguments can travel as one type.
 *
 * <p>Unlike {@link Model}, a DTO carries no identity and is exempt from
 * optimistic-lock validation -- an {@link OptimisticLockField} on a DTO is
 * ignored rather than rejected. Use {@code @Model} for types mapped back from a
 * result set, and {@code @Dto} for types that only travel inward.
 *
 * @see Model
 * @see io.github.serfom256.annotations.Param
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Dto {
}
