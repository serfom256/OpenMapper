package com.openmapper.core;

import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.exceptions.common.FunctionNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenMapperSqlContext {

    private final Map<String, FsqlEntity> context = new ConcurrentHashMap<>(32);

    public void updateContext(String name, FsqlEntity entity) {
        context.put(name, entity);
    }

    public FsqlEntity getSql(String name) {
        FsqlEntity entity = context.get(name);
        if (entity == null) {
            throw new FunctionNotFoundException(name);
        }
        return entity;
    }
}
