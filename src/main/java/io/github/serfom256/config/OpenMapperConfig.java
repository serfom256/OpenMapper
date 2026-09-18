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

/**
 * Auto-configuration entry point. Settings are read from the consuming
 * application's own {@code application.yaml}/{@code application.properties}
 * (bound to the Spring {@link Environment}); the starter no longer ships its
 * own property file.
 *
 * <p>Beans are registered explicitly rather than by {@code @ComponentScan}. An
 * auto-configuration must not scan: scanning runs against the application's bean
 * factory, so it would pick up anything that happened to sit under
 * {@code io.github.serfom256} and would register openMapper's internals in an order
 * the library does not control.
 */
@AutoConfiguration
@Import({
        OpenMapperEnvironmentConfiguration.class,
        OpenMapperQueryConfiguration.class,
        OpenMapperMappingConfiguration.class,
        JsonCodecConfiguration.class
})
public class OpenMapperConfig {

    /**
     * Registers a bean definition for each {@code @DaoLayer} interface before any
     * application bean is created, so a DAO can be injected anywhere without the
     * injecting bean having to be ordered after openMapper's own startup.
     */
    @Bean
    public static DaoLayerBeanRegistrar daoLayerBeanRegistrar() {
        return new DaoLayerBeanRegistrar();
    }

    @Bean
    public OpenMapperGlobalEnvironmentVariables openMapperGlobalEnvironmentVariables(Environment environment){
        return new OpenMapperGlobalEnvironmentVariables(environment);
    }

    /**
     * Runs every {@link EnvironmentProcessor} on construction. The bean name is
     * API: {@code @UseRepository} declares {@code @DependsOn} against it so that
     * user repositories are created only once the environment is processed.
     */
    @Bean
    public OpenMapperEnvironmentProcessor openMapperEnvironmentProcessor(List<EnvironmentProcessor> environmentProcessorList){
        return new OpenMapperEnvironmentProcessor(environmentProcessorList);
    }
}
