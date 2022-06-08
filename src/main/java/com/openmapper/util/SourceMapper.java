package com.openmapper.util;

import com.openmapper.entity.FsqlEntity;

import java.util.*;


public class SourceMapper {

    public Map<String, FsqlEntity> map(Map<String, String> parsed) {
        Map<String, FsqlEntity> map = new HashMap<>();
        for (Map.Entry<String, String> e : parsed.entrySet()) {
            String[] value = e.getValue().split(" ");
            map.put(e.getKey() , new FsqlEntity(toList(value), extractVariables(value)));
        }
        return map;
    }

    private List<String> toList(String[] sql) {
        return new ArrayList<>(Arrays.asList(sql));
    }

    private Set<String> extractVariables(String[] source) {
        Set<String> extracted = new HashSet<>();
        for (String token : source) {
            if (isVariable(token)) {
                extracted.add(token.substring(1, token.length() - 1));
            }
        }
        return extracted;
    }

    private boolean isVariable(String s) {
        return s.length() > 2 && s.startsWith("{") && s.endsWith("}");
    }
}
