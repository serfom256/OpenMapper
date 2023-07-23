package com.openmapper.common;

import com.openmapper.common.entity.SQLRecord;
import com.openmapper.common.entity.SQLToken;

import java.util.HashMap;
import java.util.Map;

public class SQLBuilder {

    public String buildSql(SQLRecord entity, Map<String, Object> toReplace) {

        final Map<String, SQLToken> tokens = entity.getVariables();
        final Map<Integer, String> replaced = new HashMap<>();

        if (tokens.size() > toReplace.size()) {
            throw new IllegalArgumentException(String.format("Invalid count of arguments given! Expected: %s given: %s", tokens.size(), toReplace.size()));
        }

        for (var tokenPair : tokens.entrySet()) {
            final Object value = toReplace.get(tokenPair.getKey());
            if (value != null) {
                replaced.put(tokenPair.getValue().getPosition(), value.toString());
            } else {
                throw new IllegalArgumentException(String.format("Argument: %s not found!", tokenPair.getKey()));
            }
        }

        final StringBuilder result = new StringBuilder();

        for (SQLToken token : entity.getSql()) {
            final String repl = replaced.get(token.getPosition());
            if (repl != null) {
                result.append(repl);
            } else {
                result.append(token.getData());
            }
        }
        return result.toString();
    }
}
