package io.github.serfom256.core.query.method;

public final class ParameterBinding {

    public enum Kind {
        PARAM,
        MODEL,
        DTO,
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
