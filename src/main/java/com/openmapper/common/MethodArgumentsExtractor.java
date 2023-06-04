package com.openmapper.common;

import com.openmapper.annotations.Param;
import com.openmapper.annotations.entity.Entity;
import com.openmapper.mappers.EntityPropertyExtractor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class MethodArgumentsExtractor {

    private MethodArgumentsExtractor() {
    }

    public static Map<String, Object> extractNamedArgs(Method method, Object[] args) {
        Parameter[] methodParams = method.getParameters();
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < methodParams.length; i++) {
            Parameter parameter = methodParams[i];
            if (parameter.getType().getAnnotation(Entity.class) != null) {
                params.putAll(EntityPropertyExtractor.extractParams(args[i]));
            } else {
                Param annotation = parameter.getAnnotation(Param.class);
                params.put(annotation == null ? parameter.getName() : annotation.name(), args[i]);
            }
        }
        return params;
    }
}
