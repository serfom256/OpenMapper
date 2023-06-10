package com.openmapper.common.mapping;

import com.openmapper.common.entity.SQLRecord;
import com.openmapper.common.entity.SQLToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SourceMapper {

    final Pattern pattern = Pattern.compile("\\[.+]", Pattern.MULTILINE);

    public SQLRecord map(final String sqlProcedure) {
        return toEntity(sqlProcedure.split(" "));
    }

    private String parseVariable(String token) {
        final Matcher matcher = pattern.matcher(token);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new IllegalArgumentException(String.format("Token: [%s] contains illegal symbols", token));
    }

    private String replaceVariable(String token, String founded) {
        return token.replace(founded, "%s");
    }

    private SQLRecord toEntity(String[] sql) {
        Map<String, SQLToken> variables = new HashMap<>();
        List<SQLToken> tokens = new ArrayList<>();
        for (String token : sql) {
            SQLToken curr;
            if (isVariable(token)) {
                String key = parseVariable(token);
                curr = new SQLToken(replaceVariable(token, key), tokens.size());
                key = key.substring(1, key.length() - 1);
                variables.put(key, curr);
            } else {
                curr = new SQLToken(token, tokens.size());
            }
            tokens.add(curr);
        }
        return new SQLRecord(tokens, variables);
    }

    private boolean isVariable(String s) {
        return s.length() > 2 && s.startsWith("[") && s.endsWith("]");
    }
}
