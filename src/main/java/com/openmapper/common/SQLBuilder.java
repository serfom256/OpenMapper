package com.openmapper.common;

import com.openmapper.core.entity.SQLRecord;
import com.openmapper.core.entity.SQLToken;

import java.util.HashMap;
import java.util.Map;

public class SQLBuilder {

    public String buildSql(SQLRecord entity, Map<String, Object> toReplace) {
        Map<String, SQLToken> tokens = entity.getVariables();
        if (tokens.size() > toReplace.size()) {
            throw new IllegalArgumentException(String.format("Invalid count of arguments given! Expected: %s given: %s", tokens.size(), toReplace.size()));
        }
        Map<Integer, String> replaced = new HashMap<>();
        for (var tokenPair : tokens.entrySet()) {
            Object value = toReplace.get(tokenPair.getKey());
            if (value != null) {
                replaced.put(tokenPair.getValue().getPosition(), String.format(tokenPair.getValue().getData(), value));
            } else {
                throw new IllegalArgumentException(String.format("Argument: %s not found!", tokenPair.getKey()));
            }
        }

        StringBuilder result = new StringBuilder();
        for (SQLToken token : entity.getSql()) {
            String repl = replaced.get(token.getPosition());
            if (repl != null) {
                result.append(repl).append(' ');
            } else {
                result.append(token.getData()).append(' ');
            }
        }
        return result.toString();
    }
}
