package com.openmapper.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import com.openmapper.core.environment.EnvironmentProcessor;
import com.openmapper.core.environment.OpenMapperEnvironmentProcessor;

@Configuration
@ComponentScan("com.openmapper")
@PropertySource("classpath:openmapper-application.properties")
public class OpenMapperConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public OpenMapperEnvironmentProcessor openMapperEnvironmentProcessor(List<EnvironmentProcessor> environmentProcessorList){
        return new OpenMapperEnvironmentProcessor(environmentProcessorList);
    }

    @Bean
    public OpenMapperGlobalEnvironmentVariables openMapperGlobalEnvironmentVariables(Environment environment){
        return new OpenMapperGlobalEnvironmentVariables(environment);
    }
}
