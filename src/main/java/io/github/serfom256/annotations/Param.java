package io.github.serfom256.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Names the bind parameter a method argument supplies, matching the placeholder
 * used in the SQL procedure.
 *
 * <p>Arguments whose type is annotated {@link io.github.serfom256.annotations.entity.Model}
 * or {@link io.github.serfom256.annotations.entity.Dto} are flattened into their own
 * fields instead, and need no {@code @Param}.
 *
 * <p>Without this annotation the argument binds under its reflective parameter
 * name, which requires the {@code -parameters} compiler flag; naming the
 * parameter explicitly avoids that dependency.
 *
 * @see io.github.serfom256.annotations.entity.Dto
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Param {

    /**
     * The bind parameter name as it appears in the SQL procedure. Left empty,
     * the reflective parameter name is used instead.
     *
     * @return the bind parameter name, or {@code ""} to use the parameter name
     */
    String name() default "";
}
