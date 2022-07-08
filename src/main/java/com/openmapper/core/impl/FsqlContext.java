package com.openmapper.core.impl;

import com.openmapper.entity.FsqlEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FsqlContext {

    private final Map<String, FsqlEntity> context;

    public FsqlContext() {
        context = new HashMap<>(32);
    }

    void updateContext(String name, FsqlEntity entity) {
        context.put(name, entity);
    }

    public FsqlEntity getSql(String name) {
        //todo throw exception if name not found
        return context.get(name);
    }
}
