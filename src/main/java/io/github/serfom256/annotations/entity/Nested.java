package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Embeds another object's {@link Field}s into the same row, in both directions.
 *
 * <p>On read, the nested object is built from columns of the row already being
 * mapped; on write, its fields are flattened into the surrounding statement's
 * bind parameters. No join and no separate query is involved -- this describes
 * one row split across two Java types, which is what separates it from
 * {@link Joined}.
 *
 * <p>Only {@code @Field}-annotated members of the nested type are mapped, and
 * nesting may go deeper than one level.
 *
 * @see Joined for a genuine association across rows
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Nested {
}
