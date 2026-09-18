package io.github.serfom256.core.query.method;

/**
 * How one DAO method parameter contributes to a query's bind parameters,
 * resolved once from its annotations rather than on every invocation.
 */
public final class ParameterBinding {

    public enum Kind {
        /** Bound directly under {@link #name()}. */
        PARAM,
        /** An {@code @Model}: flattened into its fields, with optimistic locking. */
        MODEL,
        /** A {@code @Dto}: flattened into its fields, without optimistic locking. */
        DTO,
        /** A {@link io.github.serfom256.model.page.PageRequest}: bound as limit and offset. */
        PAGE_REQUEST
    }

    private final String name;
    private final Kind kind;

    public ParameterBinding(String name, Kind kind) {
        this.name = name;
        this.kind = kind;
    }

    public String name() {
        return name;
    }

    public Kind kind() {
        return kind;
    }
}
