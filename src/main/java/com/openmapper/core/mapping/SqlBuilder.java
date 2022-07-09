package com.openmapper.core.mapping;

import com.openmapper.entity.FsqlEntity;
import com.openmapper.entity.SqlToken;

import java.util.HashMap;
import java.util.Map;

public class SqlBuilder {

    public String buildSql(FsqlEntity entity, Map<String, Object> toReplace) {
        Map<String, SqlToken> tokenMap = entity.getVariables();
        Map<Integer, String> replaced = new HashMap<>();
        for (var pair : toReplace.entrySet()) {
            SqlToken token = tokenMap.get(pair.getKey());
            if (token != null) {
                replaced.put(token.getPosition(), String.format(token.getData(), pair.getValue().toString()));
            } else {
                throw new IllegalArgumentException("");
            }
        }
        StringBuilder result = new StringBuilder();
        for (SqlToken token : entity.getSql()) {
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
