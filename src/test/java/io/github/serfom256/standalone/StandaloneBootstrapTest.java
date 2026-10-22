package io.github.serfom256.standalone;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.serfom256.OpenMapper;
import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.model.operations.DmlOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StandaloneBootstrapTest {

    private static DataSource dataSource;

    @BeforeAll
    static void startDatabase() throws Exception {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
        postgres.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(4);
        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("create table standalone_item (id bigint primary key, name varchar(64), qty int)");
            statement.execute("insert into standalone_item values (1, 'first', 10), (2, 'second', 20)");
        }
    }

    private OpenMapper openMapper() {
        return OpenMapper.builder()
                .dataSource(dataSource)
                .scanPackages("io.github.serfom256.standalone")
                .sqlLocations("classpath*:standalone/*.fsql")
                .build();
    }

    @Test
    @DisplayName("reads through a DAO with no ApplicationContext")
    void readsWithoutSpringContainer() {
        ItemDao items = openMapper().dao(ItemDao.class);

        List<Item> all = items.findAllItems();
        Item one = items.findItemById(2L);

        assertThat(all).extracting(Item::getName).contains("first", "second");
        assertThat(one.getName()).isEqualTo("second");
        assertThat(one.getQty()).isEqualTo(20);
    }

    @Test
    @DisplayName("writes through a DAO with no ApplicationContext")
    void writesWithoutSpringContainer() {
        ItemDao items = openMapper().dao(ItemDao.class);

        Item item = new Item();
        item.id = 99;
        item.name = "written";
        item.qty = 7;
        items.insertItem(item);

        assertThat(items.findItemById(99L).getName()).isEqualTo("written");
    }

    @Test
    @DisplayName("a rollback undoes a write with no ApplicationContext")
    void rollbackWithoutSpringContainer() {
        ItemDao items = openMapper().dao(ItemDao.class);
        TransactionTemplate transactions =
                new TransactionTemplate(new DataSourceTransactionManager(dataSource));

        assertThatThrownBy(() -> transactions.execute(status -> {
            Item item = new Item();
            item.id = 123;
            item.name = "rolled-back";
            item.qty = 1;
            items.insertItem(item);
            throw new IllegalStateException("forced rollback");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(items.findItemById(123L)).isNull();
    }

    @Test
    @DisplayName("the same DAO instance is reused")
    void cachesDaoProxies() {
        OpenMapper openMapper = openMapper();

        assertThat(openMapper.dao(ItemDao.class)).isSameAs(openMapper.dao(ItemDao.class));
    }

    @Test
    @DisplayName("a missing data source is reported before anything is scanned")
    void requiresDataSource() {
        assertThatThrownBy(() -> OpenMapper.builder().scanPackages("io.github.serfom256.standalone").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("data source");
    }

    @Test
    @DisplayName("a missing package to scan is reported")
    void requiresPackagesToScan() {
        assertThatThrownBy(() -> OpenMapper.builder().dataSource(dataSource).build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("package to scan");
    }

    @Model(primaryKey = "id")
    public static class Item {
        @Field(name = "id")
        private long id;
        @Field(name = "name")
        private String name;
        @Field(name = "qty")
        private int qty;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getQty() {
            return qty;
        }
    }

    @DaoLayer
    public interface ItemDao {

        @DaoMethod(operation = DmlOperation.SELECT)
        List<Item> findAllItems();

        @DaoMethod(operation = DmlOperation.SELECT)
        Item findItemById(@Param(name = "id") Long id);

        @DaoMethod(operation = DmlOperation.INSERT)
        void insertItem(Item item);
    }
}
