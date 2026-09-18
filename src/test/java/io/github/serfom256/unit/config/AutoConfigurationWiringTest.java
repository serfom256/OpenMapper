package io.github.serfom256.unit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.serfom256.config.OpenMapperConfig;
import io.github.serfom256.core.environment.EnvironmentProcessor;
import io.github.serfom256.core.environment.processors.DaoMethodEnvironmentProcessor;
import io.github.serfom256.core.environment.processors.ModelEnvironmentProcessor;
import io.github.serfom256.core.environment.processors.ProcedureSourceEnvironmentProcessor;
import io.github.serfom256.core.query.facade.QueryFacade;
import io.github.serfom256.mappers.ModelExtractor;

/**
 * Guards the auto-configuration's wiring contract. openMapper registers its beans
 * explicitly rather than by component scanning, so the things scanning used to
 * provide implicitly -- that every bean resolves, that the environment processors
 * run in a fixed order, and that bean names stay stable -- are now this class's
 * responsibility to pin down.
 */
class AutoConfigurationWiringTest {

    // Packages are configured explicitly. Left unset, PackageScanner falls back to
    // locating the @SpringBootApplication bean, which a bare context runner has no
    // reason to contain.
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenMapperConfig.class))
            .withPropertyValues(
                    "openmapper.dao.packagesToScan=io.github.serfom256.unit.config",
                    "openmapper.model.packagesToScan=io.github.serfom256.unit.config");

    @Test
    @DisplayName("auto-configuration wires a complete context with no DAOs present")
    void contextLoads() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(QueryFacade.class);
            assertThat(context).hasSingleBean(ModelExtractor.class);
        });
    }

    @Test
    @DisplayName("environment processors run in a fixed, declared order")
    void environmentProcessorsAreOrdered() {
        runner.run(context -> {
            List<EnvironmentProcessor> processors =
                    context.getBeanProvider(EnvironmentProcessor.class).orderedStream().toList();
            assertThat(processors).extracting(p -> p.getClass().getName())
                    .containsExactly(
                            DaoMethodEnvironmentProcessor.class.getName(),
                            ModelEnvironmentProcessor.class.getName(),
                            ProcedureSourceEnvironmentProcessor.class.getName());
        });
    }

    @Test
    @DisplayName("the bean name @UseRepository depends on is part of the public contract")
    void environmentProcessorBeanNameIsStable() {
        // @UseRepository is meta-annotated @DependsOn("openMapperEnvironmentProcessor"),
        // so renaming this bean silently breaks every consumer using that annotation.
        runner.run(context -> assertThat(context).hasBean("openMapperEnvironmentProcessor"));
    }

    @Test
    @DisplayName("no Serializer on the classpath is tolerated (Jackson is optional)")
    void jsonCodecIsOptional() {
        runner.withClassLoader(new org.springframework.boot.test.context.FilteredClassLoader(
                        "com.fasterxml.jackson.databind.ObjectMapper",
                        "tools.jackson.databind.json.JsonMapper"))
              .run(context -> {
                  assertThat(context).hasNotFailed();
                  assertThat(context).hasSingleBean(ModelExtractor.class);
              });
    }
}
