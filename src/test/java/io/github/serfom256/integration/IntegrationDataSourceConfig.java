package io.github.serfom256.integration;

import io.github.serfom256.benchmark.DatabaseSetup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Supplies the Testcontainers-backed DataSource to the integration test context.
 * Providing this bean makes Spring Boot's DataSourceAutoConfiguration back off,
 * and {@link DatabaseSetup#initialize()} ensures the schema and seed data exist
 * before the DataSource is used. Gated to the {@code it} profile so it never
 * leaks into the benchmark contexts.
 */
@TestConfiguration
@Profile("it")
public class IntegrationDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        DatabaseSetup.initialize();
        return DatabaseSetup.getDataSource();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
