package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the version field guarding a model against concurrent modification. At
 * most one per {@link Model}; declaring more is rejected at startup with a
 * {@link io.github.serfom256.exceptions.entity.ModelValidationException}.
 *
 * <p>The field participates in statements that may overwrite an existing row --
 * {@link io.github.serfom256.model.operations.DmlOperation#UPDATE} and
 * {@link io.github.serfom256.model.operations.DmlOperation#MERGE}. For those
 * openMapper binds two parameters: the incremented value under
 * {@link #name()}, and the value the entity was loaded with under
 * {@link #oldVersion()}. Write the {@code WHERE} clause against the latter so
 * the update matches no rows when another writer has moved the version on. For
 * any other operation the field binds as an ordinary column.
 *
 * <p>Supported types: {@code int}, {@code Integer}, {@code short},
 * {@code Short}, {@code long}, {@code Long}.
 *
 * <p>On a {@link Dto} the annotation is ignored.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface OptimisticLockField {

    /**
     * Name of the version column, carrying the newly incremented value on an
     * update. Defaults to the Java field name when left empty.
     *
     * @return the version column name, or {@code ""} to use the field name
     */
    String name() default "";

    /**
     * Name of the bind parameter carrying the <em>previous</em> version -- the
     * value the entity was loaded with. Reference it in the statement's
     * {@code WHERE} clause to make the update conditional.
     *
     * @return the bind parameter name for the previous version
     */
    String oldVersion();
}
