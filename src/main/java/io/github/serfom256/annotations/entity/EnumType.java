package io.github.serfom256.annotations.entity;

/**
 * How an enum field is stored in its column.
 *
 * @see Field#enumType()
 */
public enum EnumType {

    /** Stored as the constant's name, in a text column. */
    NAME,

    /**
     * Stored as the constant's position, in a numeric column.
     *
     * <p>Reordering or removing a constant silently changes what existing rows
     * mean, so prefer {@link #NAME} for new schemas; this exists for tables that
     * already hold ordinals.
     */
    ORDINAL
}
