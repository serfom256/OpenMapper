package com.openmapper.parser.model;

public class CharLocation {
    private final int line;
    private final int column;
    private final int offset;

    public CharLocation(int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getOffset() {
        return offset;
    }
}