package io.github.serfom256.config;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import io.github.serfom256.core.environment.DaoLayerBeanRegistrar;
import io.github.serfom256.core.environment.EnvironmentProcessor;
import io.github.serfom256.core.environment.OpenMapperEnvironmentProcessor;
import io.github.serfom256.json.JsonCodecConfiguration;

@AutoConfiguration
@Import({
        OpenMapperEnvironmentConfiguration.class,
        OpenMapperQueryConfiguration.class,
        OpenMapperMappingConfiguration.class,
        JsonCodecConfiguration.class
})
public class OpenMapperConfig {

    @Bean
    public static DaoLayerBeanRegistrar daoLayerBeanRegistrar() {
        return new DaoLayerBeanRegistrar();
    }

    @Bean
    public OpenMapperGlobalEnvironmentVariables openMapperGlobalEnvironmentVariables(Environment environment){
        return new OpenMapperGlobalEnvironmentVariables(environment);
    }

    @Bean
    public OpenMapperEnvironmentProcessor openMapperEnvironmentProcessor(List<EnvironmentProcessor> environmentProcessorList){
        return new OpenMapperEnvironmentProcessor(environmentProcessorList);
    }
}
