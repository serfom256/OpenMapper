package com.openmapper.common;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParsedObjectsFormatter {

    public void format(Map<String, String> parsed) {
        for (var pair : parsed.entrySet()) {
            pair.setValue(formatSql(pair.getValue()));
        }
    }

    private String formatSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

}
