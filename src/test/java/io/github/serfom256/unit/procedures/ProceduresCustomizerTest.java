package io.github.serfom256.unit.procedures;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.github.serfom256.config.OpenMapperConfig;
import io.github.serfom256.procedures.Procedures;
import io.github.serfom256.procedures.ProceduresCustomizer;

class ProceduresCustomizerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenMapperConfig.class))
            .withPropertyValues(
                    "openmapper.dao.packagesToScan=io.github.serfom256.unit.procedures",
                    "openmapper.model.packagesToScan=io.github.serfom256.unit.procedures");

    @Test
    @DisplayName("Procedures is available without a DataSource in the context")
    void availableWithoutDataSource() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(Procedures.class);
        });
    }

    @Test
    @DisplayName("every customizer bean is applied, in order")
    void customizersAreAppliedInOrder() {
        runner.withUserConfiguration(CustomizerConfig.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(Procedures.class)).isNotNull();
            assertThat(CustomizerConfig.applied).containsExactly("first", "second");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomizerConfig {

        static final java.util.List<String> applied = new java.util.ArrayList<>();

        @Bean
        DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        @Order(1)
        ProceduresCustomizer first() {
            return builder -> {
                applied.add("first");
                builder.fetchSize(100).queryTimeoutSeconds(5).statementCacheSize(16);
            };
        }

        @Bean
        @Order(2)
        ProceduresCustomizer second() {
            return builder -> {
                applied.add("second");
                builder.fetchSize(250);
            };
        }
    }
}
