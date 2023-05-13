package com.openmapper.core.resources.mapping;

import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.entity.SqlToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class SourceMapper {

    final Pattern pattern = Pattern.compile("\\[.+]", Pattern.MULTILINE);

    public Map<String, FsqlEntity> map(Map<String, String> parsed) {
        Map<String, FsqlEntity> map = new HashMap<>();
        for (Map.Entry<String, String> e : parsed.entrySet()) {
            String[] value = e.getValue().split(" ");
            map.put(e.getKey(), toEntity(value));
        }
        return map;
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

    private FsqlEntity toEntity(String[] sql) {
        Map<String, SqlToken> variables = new HashMap<>();
        List<SqlToken> tokens = new ArrayList<>();
        for (String token : sql) {
            SqlToken curr;
            if (isVariable(token)) {
                String key = parseVariable(token);
                curr = new SqlToken(replaceVariable(token, key), tokens.size());
                key = key.substring(1, key.length() - 1);
                variables.put(key, curr);
            } else {
                curr = new SqlToken(token, tokens.size());
            }
            tokens.add(curr);
        }
        return new FsqlEntity(tokens, variables);
    }

    private boolean isVariable(String s) {
        return s.length() > 2 && s.contains("[") && s.contains("]");
    }
}
