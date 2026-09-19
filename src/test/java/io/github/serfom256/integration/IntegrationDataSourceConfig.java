package io.github.serfom256.integration;

import io.github.serfom256.benchmark.DatabaseSetup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

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
