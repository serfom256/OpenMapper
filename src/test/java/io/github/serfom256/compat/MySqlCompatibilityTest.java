package io.github.serfom256.compat;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;

class MySqlCompatibilityTest extends AbstractDialectCompatibilityTest {

    @Override
    protected JdbcDatabaseContainer<?> container() {
        return new MySQLContainer<>("mysql:8.4");
    }

    @Override
    protected String createTable() {
        return "create table compat_item (id bigint primary key, name varchar(64), qty int)";
    }

    @Override
    protected String createKeyedTable() {
        return "create table compat_keyed (id bigint auto_increment primary key, name varchar(64))";
    }
}
