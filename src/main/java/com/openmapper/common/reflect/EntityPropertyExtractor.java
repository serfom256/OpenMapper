package com.openmapper.common.reflect;

import com.openmapper.annotations.Param;
import com.openmapper.annotations.entity.Model;
import com.openmapper.mappers.MethodArgumentExtractor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class EntityPropertyExtractor {

    private final MethodArgumentExtractor methodArgumentExtractor;

    public EntityPropertyExtractor(MethodArgumentExtractor methodArgumentExtractor) {
        this.methodArgumentExtractor = methodArgumentExtractor;
    }

    public Map<String, Object> extract(Method method, Object[] args) {
        final Parameter[] methodParams = method.getParameters();
        final Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < methodParams.length; i++) {
            final Parameter parameter = methodParams[i];
            if (parameter.getType().getAnnotation(Model.class) != null) {
                params.putAll(methodArgumentExtractor.extractMethodArguments(args[i]));
            } else {
                final Param annotation = parameter.getAnnotation(Param.class);
                params.put(annotation == null ? parameter.getName() : annotation.name(), args[i]);
            }
        }
        return params;
    }
}
