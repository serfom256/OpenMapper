package io.github.serfom256.unit.daobeans;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.serfom256.config.OpenMapperConfig;

/**
 * A {@code @DaoLayer} interface must be injectable into any bean, whatever order
 * the container happens to create beans in. openMapper used to register DAO
 * proxies while the context was already instantiating beans, so a bean created
 * before openMapper's own startup bean failed with NoSuchBeanDefinitionException
 * -- which is what a plain {@code @Repository} in Broadleaf Commerce hit.
 */
class DaoInjectionOrderTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenMapperConfig.class))
            .withPropertyValues(
                    "openmapper.dao.packagesToScan=io.github.serfom256.unit.daobeans",
                    "openmapper.model.packagesToScan=io.github.serfom256.unit.daobeans")
            .withUserConfiguration(ConsumerConfiguration.class);

    @Test
    @DisplayName("a DAO can be injected into a bean that does not depend on openMapper's startup")
    void daoIsInjectableIntoAnyBean() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(EarlyConsumer.class).dao()).isNotNull();
        });
    }

    @Test
    @DisplayName("a DAO proxy behaves as an object: equals, hashCode and toString do not hit the database")
    void daoProxyIsAWellBehavedObject() {
        runner.run(context -> {
            EarlyDao dao = context.getBean(EarlyDao.class);
            assertThat(dao).isEqualTo(dao);
            assertThat(dao.hashCode()).isEqualTo(System.identityHashCode(dao));
            assertThat(dao.toString()).contains("EarlyDao");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class ConsumerConfiguration {

        @Bean
        DataSource dataSource() {
            return Mockito.mock(DataSource.class);
        }

        @Bean
        EarlyConsumer earlyConsumer(EarlyDao dao) {
            return new EarlyConsumer(dao);
        }
    }
}
