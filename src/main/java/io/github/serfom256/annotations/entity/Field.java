package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field to a single result-set column, in both directions: the column is
 * read into the field when mapping a row, and the field's value is bound as a
 * parameter of the same name when the model is passed to a DAO method.
 *
 * <p>Annotate the field with {@link Json} as well when the column holds JSON.
 *
 * @see Json
 * @see Nested for flattening another object into the same row
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Field {

    /**
     * The column and bind-parameter name. Defaults to the Java field name when
     * left empty.
     *
     * @return the column name, or {@code ""} to use the field name
     */
    String name() default "";

    /**
     * Whether the column is only ever read, never bound when the model is written.
     *
     * <p>Use it for values the database produces: an expression computed by the
     * query, a column with a {@code DEFAULT}, one maintained by a trigger, or an
     * aggregate selected alongside the entity.
     *
     * <pre>{@code
     * @Field(name = "book_count", readOnly = true)
     * private int bookCount;
     * }</pre>
     *
     * <p>A read-only field is mapped exactly like any other on the way in; it is
     * simply absent from the parameters on the way out, so the same model can be
     * used for both without the insert referring to a column it cannot set.
     *
     * @return {@code true} to exclude the field from writes
     */
    boolean readOnly() default false;

    /**
     * How an enum field is stored. Ignored for every other type.
     *
     * @return {@link EnumType#NAME} by default
     */
    EnumType enumType() default EnumType.NAME;
}
