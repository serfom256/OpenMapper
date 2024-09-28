package com.openmapper.parser.model;

public class SQLProcedure {
    private final String functionName;
    private final String functionBody;

    public SQLProcedure(String functionName, String functionBody) {
        this.functionName = functionName;
        this.functionBody = functionBody;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getFunctionBody() {
        return functionBody;
    }

    @Override
    public String toString() {
        return "SQLProcedure{" +
                "functionName='" + functionName + '\'' +
                ", functionBody='" + functionBody + '\'' +
                '}';
    }
}
