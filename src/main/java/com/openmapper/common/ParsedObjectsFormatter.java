package com.openmapper.common;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParsedObjectsFormatter {

    public void format(Map<String, String> parsed) {
        for (Map.Entry<String, String> pair : parsed.entrySet()) {
            pair.setValue(normalizeSql(pair.getValue()));
        }
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

}
