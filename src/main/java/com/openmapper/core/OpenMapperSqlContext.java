package com.openmapper.core;

import com.openmapper.core.entity.SQLRecord;
import com.openmapper.exceptions.common.FunctionNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenMapperSqlContext {

    private final Map<String, SQLRecord> context = new ConcurrentHashMap<>(32);

    public void updateContext(String name, SQLRecord entity) {
        context.put(name, entity);
    }

    public SQLRecord getSql(String name) {
        SQLRecord entity = context.get(name);
        if (entity == null) {
            throw new FunctionNotFoundException(name);
        }
        return entity;
    }
}
