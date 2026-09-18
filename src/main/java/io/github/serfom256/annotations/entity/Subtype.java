package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * One concrete type of an {@link Inheritance} hierarchy, and the discriminator
 * value that selects it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Subtypes.class)
public @interface Subtype {

    /**
     * @return the discriminator value found in the column
     */
    String value();

    /**
     * @return the type to build when the column holds that value
     */
    Class<?> type();
}
