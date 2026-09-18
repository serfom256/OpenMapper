package io.github.serfom256.annotations;

import io.github.serfom256.exceptions.entity.NoTranslationException;
import io.github.serfom256.model.operations.DmlOperation;

import static io.github.serfom256.model.operations.DmlOperation.SELECT;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method on a {@link DaoLayer} interface to a SQL procedure and declares
 * how its result should be treated.
 *
 * <p>The method's return type drives result handling on its own: a model type
 * maps one row, an {@link Iterable} maps many, {@code void} discards the result,
 * and {@link java.util.Optional} is unwrapped to its element type.
 *
 * <p>The annotation is optional. An unannotated method on a {@code @DaoLayer}
 * interface is treated as a {@link DmlOperation#SELECT} whose procedure name is
 * the method name.
 *
 * @see DaoLayer
 * @see io.github.serfom256.annotations.entity.Sql
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DaoMethod {

    /**
     * Name of the procedure to execute, as declared in a {@code .fsql} file on
     * the configured {@code openmapper.fsql.path}. When empty, the method name
     * is used instead.
     *
     * @return the procedure name, or {@code ""} to use the method name
     */
    String procedure() default "";

    /**
     * The kind of statement this method runs. This selects the execution path,
     * and {@link DmlOperation#UPDATE} or {@link DmlOperation#MERGE} additionally
     * enable optimistic locking for models carrying an
     * {@link io.github.serfom256.annotations.entity.OptimisticLockField}.
     *
     * @return the DML operation, {@link DmlOperation#SELECT} by default
     */
    DmlOperation operation() default SELECT;

    /**
     * Whether to request database-generated keys for this statement, mapping the
     * generated-keys result set instead of the update count. Use it on inserts
     * that need to return an assigned identifier.
     *
     * @return {@code true} to return generated keys
     */
    boolean returnKeys() default false;

    /**
     * Throws this exception instead of returning {@code null} when the query
     * matches no row.
     *
     * <p>Opt-in: left at its default the method keeps returning {@code null}.
     * Set it to
     * {@link io.github.serfom256.exceptions.entity.EntityNotFoundException} for the
     * conventional behaviour, or to any exception of your own.
     *
     * <p>Return types that already express absence are never translated --
     * {@link java.util.Optional}, collections and arrays come back empty
     * instead.
     *
     * <p>The type needs a public {@code (String)} or no-argument constructor.
     * Prefer an unchecked exception: a checked one propagates only if the method
     * also declares it in a {@code throws} clause, because a JDK proxy would
     * otherwise replace it with an
     * {@link java.lang.reflect.UndeclaredThrowableException}.
     *
     * @return the exception to throw when nothing is found, or
     *         {@link NoTranslationException} to return {@code null}
     */
    Class<? extends Throwable> translateNotFoundExceptionTo() default NoTranslationException.class;

    /**
     * Rows the driver fetches per round trip. Zero leaves the driver's default in
     * place.
     *
     * <p>Set this for queries returning many rows: PostgreSQL otherwise reads the
     * whole result set into memory before the first row is handed back. It takes
     * effect only inside a transaction, because the driver needs an open cursor.
     *
     * @return the fetch size, or 0 for the driver default
     */
    /**
     * Procedure that counts the rows the paged query matches, required when the
     * method returns a {@link io.github.serfom256.model.page.Page}. It runs as a second
     * statement and fills in the page total.
     *
     * @return the counting procedure's name
     */
    String countProcedure() default "";

    int fetchSize() default 0;

    /**
     * Seconds the driver waits for this statement before cancelling it. Zero
     * leaves the driver's default, which is usually no limit.
     *
     * @return the timeout in seconds, or 0 for no limit
     */
    int queryTimeoutSeconds() default 0;
}
