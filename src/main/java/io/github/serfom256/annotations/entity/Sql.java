package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link String} parameter as carrying the statement to execute, instead
 * of naming one declared in a {@code .fsql} file.
 *
 * <p>A method with such a parameter bypasses procedure lookup entirely, taking
 * its SQL from the argument at call time. The parsed result is still cached by
 * statement text, so repeatedly passing the same SQL parses it once.
 *
 * <p>Exactly one parameter must carry this annotation on such a method;
 * otherwise the call fails with an {@link IllegalStateException}.
 *
 * <p><strong>Only pass SQL your application controls.</strong> Everything else
 * openMapper runs is a fixed statement with bound parameters; text arriving here
 * is executed as given, so building it from user input reintroduces the
 * injection risk that parameter binding otherwise removes.
 *
 * @see io.github.serfom256.annotations.DaoMethod#procedure()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Sql {
}
