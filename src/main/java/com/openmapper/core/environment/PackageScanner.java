package com.openmapper.core.environment;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;


public class PackageScanner {

    public Set<Class<?>> scanPackagesFor(String packageToScan, Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(packageToScan);
        return reflections.getTypesAnnotatedWith(annotation);
    }
}
