package com.openmapper.core.environment;


import java.util.List;

public class OpenMapperEnvironmentProcessor {
    public OpenMapperEnvironmentProcessor(List<EnvironmentProcessor> loaders) {
        loaders.forEach(EnvironmentProcessor::processEnvironment);
    }
}
