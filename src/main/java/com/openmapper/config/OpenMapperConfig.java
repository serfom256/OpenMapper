package com.openmapper.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ComponentScan("com.openmapper")
public class OpenMapperConfig {

    private final Logger logger = LoggerFactory.getLogger(OpenMapperConfig.class);

    @PostConstruct
    private void init() {
//        final String property = environment.getProperty(SQL_TRACING.getValue());
//        if (Boolean.parseBoolean(property == null ? "false" : property)) {
//            logger.info("OpenMapper started");
//        }
    }

}
