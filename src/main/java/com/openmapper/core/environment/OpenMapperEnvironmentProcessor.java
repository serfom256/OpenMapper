package com.openmapper.core.environment;

import java.util.List;

public class OpenMapperEnvironmentProcessor {
    public OpenMapperEnvironmentProcessor(List<EnvironmentProcessor> processors) {
        processors.forEach(EnvironmentProcessor::processEnvironment);
    }
}
