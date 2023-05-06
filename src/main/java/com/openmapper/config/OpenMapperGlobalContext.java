package com.openmapper.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.LOGGING;
import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.SQL_TRACING;

@Component
public class OpenMapperGlobalContext {


    private final Environment environment;

    public OpenMapperGlobalContext(Environment environment) {
        this.environment = environment;
    }

    public boolean isLogging() {
        final String property = environment.getProperty(LOGGING.getValue());
        return Boolean.parseBoolean(property == null ? "false" : property);
    }

    public boolean isSqlTracing() {
        final String property = environment.getProperty(SQL_TRACING.getValue());
        return Boolean.parseBoolean(property == null ? "false" : property);
    }
}
