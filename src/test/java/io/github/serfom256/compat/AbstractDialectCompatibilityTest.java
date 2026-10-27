package io.github.serfom256.compat;

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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("compat")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractDialectCompatibilityTest {

    private CompatDao items;
    private OpenMapper openMapper;

    protected abstract JdbcDatabaseContainer<?> container();

    protected abstract String createTable();

    protected abstract String createKeyedTable();

    @BeforeAll
    void startAndSeed() throws Exception {
        JdbcDatabaseContainer<?> container = container();
        container.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());
        config.setMaximumPoolSize(3);
        DataSource dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(createTable());
            statement.execute(createKeyedTable());
            statement.execute("insert into compat_item (id, name, qty) values (1, 'first', 10)");
            statement.execute("insert into compat_item (id, name, qty) values (2, 'second', 20)");
        }

        openMapper = OpenMapper.builder()
                .dataSource(dataSource)
                .scanPackages("io.github.serfom256.compat")
                .sqlLocations("classpath*:compat/*.fsql")
                .build();
        items = openMapper.dao(CompatDao.class);
    }

    @Test
    @DisplayName("maps rows whatever case the driver reports column labels in")
    void mapsRowsRegardlessOfLabelCase() {
        List<CompatItem> seeded = items.findAllCompat().stream()
                .filter(item -> item.getId() <= 2)
                .toList();

        assertThat(seeded).extracting(CompatItem::getId).containsExactly(1L, 2L);
        assertThat(seeded).extracting(CompatItem::getName).containsExactly("first", "second");
        assertThat(seeded).extracting(CompatItem::getQty).containsExactly(10, 20);
    }

    @Test
    @DisplayName("binds a parameter and maps a single row")
    void readsSingleRow() {
        CompatItem item = items.findCompatById(2L);

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("second");
        assertThat(item.getQty()).isEqualTo(20);
    }

    @Test
    @DisplayName("writes a row")
    void writesRow() {
        CompatItem item = new CompatItem();
        item.id = 50;
        item.name = "written";
        item.qty = 5;
        items.insertCompat(item);

        assertThat(items.findCompatById(50L).getName()).isEqualTo("written");
    }

    @Test
    @DisplayName("binds a null parameter")
    void bindsNullParameter() {
        CompatItem item = new CompatItem();
        item.id = 51;
        item.name = null;
        item.qty = 7;
        items.insertCompat(item);

        CompatItem found = items.findCompatById(51L);
        assertThat(found.getName()).isNull();
        assertThat(found.getQty()).isEqualTo(7);
    }

    @Test
    @DisplayName("returns the generated key, not a driver row identifier")
    void returnsGeneratedKey() {
        KeyedItem item = new KeyedItem();
        item.name = "generated";

        long key = items.insertKeyed(item);

        assertThat(key).isPositive();
        assertThat(items.findKeyedById(key).getName()).isEqualTo("generated");
    }

    @Test
    @DisplayName("returns a generated key for every element of a batch")
    void returnsGeneratedKeysForBatch() {
        KeyedItem first = new KeyedItem();
        first.name = "batch-one";
        KeyedItem second = new KeyedItem();
        second.name = "batch-two";

        List<Long> keys = items.insertKeyedBatch(List.of(first, second));

        assertThat(keys).hasSize(2);
        assertThat(keys).allSatisfy(key -> assertThat(key).isPositive());
        assertThat(items.findKeyedById(keys.get(0)).getName()).isEqualTo("batch-one");
        assertThat(items.findKeyedById(keys.get(1)).getName()).isEqualTo("batch-two");
    }

    @Test
    @DisplayName("a procedure returns the generated key read from a named column")
    void procedureReturnsGeneratedKeyFromNamedColumn() {
        Long key = openMapper.procedures().<Long>executeReturningKey(
                "INSERT INTO compat_keyed (name) VALUES ([name])",
                DmlOperation.INSERT, Long.class, Map.of("name", "procedure-keyed"), "id").orElseThrow();

        assertThat(key).isPositive();
        assertThat(items.findKeyedById(key).getName()).isEqualTo("procedure-keyed");
    }

    @Model(primaryKey = "id")
    public static class KeyedItem {
        @Field(name = "id")
        private long id;
        @Field(name = "name")
        private String name;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    @Model(primaryKey = "id")
    public static class CompatItem {
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
    public interface CompatDao {

        @DaoMethod(operation = DmlOperation.SELECT)
        List<CompatItem> findAllCompat();

        @DaoMethod(operation = DmlOperation.SELECT)
        CompatItem findCompatById(@Param(name = "id") Long id);

        @DaoMethod(operation = DmlOperation.INSERT)
        void insertCompat(CompatItem item);

        @DaoMethod(operation = DmlOperation.INSERT, returnKeys = true)
        long insertKeyed(KeyedItem item);

        @DaoMethod(procedure = "insertKeyed", operation = DmlOperation.INSERT, returnKeys = true)
        List<Long> insertKeyedBatch(List<KeyedItem> items);

        @DaoMethod(operation = DmlOperation.SELECT)
        KeyedItem findKeyedById(@Param(name = "id") Long id);
    }
}
