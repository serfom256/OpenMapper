package com.openmapper.core.environment;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenMapperEnvironmentLoader {
    public OpenMapperEnvironmentLoader(List<OpenMapperEnvironmentProcessor> loaders) {
        loaders.forEach(OpenMapperEnvironmentProcessor::processEnvironment);
    }
}
