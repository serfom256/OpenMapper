package io.github.serfom256.compat;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;

class OracleCompatibilityTest extends AbstractDialectCompatibilityTest {

    @Override
    protected JdbcDatabaseContainer<?> container() {
        return new OracleContainer("gvenzl/oracle-xe:21.3.0-slim-faststart");
    }

    @Override
    protected String createTable() {
        return "create table compat_item (id number(19) primary key, name varchar2(64), qty number(10))";
    }

    @Override
    protected String createKeyedTable() {
        return "create table compat_keyed (id number(19) generated always as identity primary key, name varchar2(64))";
    }
}
