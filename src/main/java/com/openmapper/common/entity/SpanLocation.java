package com.openmapper.common.entity;

public class SpanLocation {
    private final CharLocation start;
    private final CharLocation end;

    public SpanLocation(CharLocation start, CharLocation end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return start.toString() + "-" + end.toString();
    }

    public CharLocation getStart() {
        return start;
    }

    public CharLocation getEnd() {
        return end;
    }
}