package com.openmapper.core.environment;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GlobalEnvironmentProcessor {
    public GlobalEnvironmentProcessor(List<EnvironmentProcessor> loaders) {
        loaders.forEach(EnvironmentProcessor::processEnvironment);
    }
}