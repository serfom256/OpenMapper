package io.github.serfom256.annotations.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link Model} whose rows may map to one of several concrete types,
 * chosen per row by the value of a discriminator column.
 *
 * <p>Pair it with a {@link Subtype} for each concrete type:
 *
 * <pre>{@code
 * @Model(primaryKey = "id")
 * @Inheritance(discriminator = "product_type")
 * @Subtype(value = "BUNDLE", type = BundleProduct.class)
 * @Subtype(value = "SKU", type = SkuProduct.class)
 * public class Product { ... }
 * }</pre>
 *
 * <p>Subtypes inherit the base type's mapped fields, so only their own columns
 * need declaring. Which tables the query reads is up to the SQL: one table with
 * a discriminator column, or a base table outer-joined to per-subtype tables,
 * both work the same way here.
 *
 * <p>A row whose discriminator is SQL NULL maps to the base type, which is what
 * an outer join produces when no subtype row matched. An unrecognised value is
 * an error rather than a silent fallback.
 *
 * <p>Writing is not polymorphic: the procedure a DAO method names decides where
 * a row goes, as for any other write.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Inheritance {

    /**
     * Column holding the value that identifies the concrete type.
     *
     * @return the discriminator column name
     */
    String discriminator();
}
