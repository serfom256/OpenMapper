package com.openmapper.core;

import com.openmapper.entity.FsqlEntity;

import java.util.Collections;
import java.util.Map;

public class OpenMapperContext {

    private final Map<String, FsqlEntity> context;

    public OpenMapperContext(Map<String, FsqlEntity> context) {
        this.context = Collections.unmodifiableMap(context);
    }

    public FsqlEntity getData(String name) {
        return context.get(name);
    }
}
