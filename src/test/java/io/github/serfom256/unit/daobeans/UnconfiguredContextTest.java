package io.github.serfom256.unit.daobeans;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import io.github.serfom256.config.OpenMapperConfig;

class UnconfiguredContextTest {

    @Test
    @DisplayName("a context with no scan package and no @SpringBootApplication still starts")
    void unconfiguredContextStarts() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OpenMapperConfig.class))
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBeanNamesForType(EarlyDao.class)).isEmpty();
                });
    }
}
