package io.github.serfom256.standalone;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.annotations.Param;
import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.Sql;
import io.github.serfom256.cache.QueryCacheImpl;
import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.proxy.InvocationProxy;
import io.github.serfom256.core.query.common.NotFoundTranslator;
import io.github.serfom256.core.query.common.PreparedStatementActions;
import io.github.serfom256.core.query.executors.JdbcQueryExecutor;
import io.github.serfom256.core.query.executors.QueryExecutionStrategy;
import io.github.serfom256.core.query.executors.operations.DatabaseOperation;
import io.github.serfom256.core.query.executors.operations.DmlSelectOperationExecutor;
import io.github.serfom256.core.query.executors.operations.DmlUpdateOperationExecutor;
import io.github.serfom256.core.query.facade.AssociationCascade;
import io.github.serfom256.core.query.facade.OpenMapperCachingFacade;
import io.github.serfom256.core.query.facade.QueryFacadeImpl;
import io.github.serfom256.core.query.handlers.EmptyResultSetHandler;
import io.github.serfom256.core.query.handlers.EntityResultSetHandler;
import io.github.serfom256.core.query.handlers.IterableResultSetHandler;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.core.query.method.DaoMethodPlanRegistry;
import io.github.serfom256.core.proxy.QueryMappingInvocationHandler;
import io.github.serfom256.core.query.source.mapping.impl.InputMapperImpl;
import io.github.serfom256.mappers.ColumnTypeRegistry;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;
import io.github.serfom256.common.reflect.FieldValueGenerator;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.common.DaoMethodFacade;
import io.github.serfom256.model.operations.DmlOperation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    @DisplayName("openMapper runs with no ApplicationContext, wired by hand")
    void worksWithoutSpringContainer() throws Exception {
        ItemDao dao = bootstrap();

        List<Item> all = dao.findAll();
        Item one = dao.findById(2L);

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Item::getName).containsExactlyInAnyOrder("first", "second");
        assertThat(one.getName()).isEqualTo("second");
        assertThat(one.getQty()).isEqualTo(20);
    }

    private ItemDao bootstrap() throws Exception {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("openmapper",
                Map.of("openmapper.logging.enabled", "false")));
        OpenMapperGlobalEnvironmentVariables variables = new OpenMapperGlobalEnvironmentVariables(environment);

        ColumnTypeRegistry columnTypes = new ColumnTypeRegistry();
        MappingPlanRegistry mappingPlans = new MappingPlanRegistry(null, columnTypes);
        ResultSetGraphReader graphReader = new ResultSetGraphReader(mappingPlans);
        ResultSetObjectMapper objectMapper = new ResultSetObjectMapper(graphReader);
        ResultSetPrimitiveMapper primitiveMapper = new ResultSetPrimitiveMapper();

        EntityResultSetHandler entityHandler = new EntityResultSetHandler(objectMapper, primitiveMapper);
        IterableResultSetHandler iterableHandler = new IterableResultSetHandler(objectMapper, primitiveMapper);
        ResultSetHandlerStrategy handlers =
                new ResultSetHandlerStrategy(entityHandler, new EmptyResultSetHandler(), iterableHandler);

        PreparedStatementActions actions = new PreparedStatementActions();
        QueryExecutionStrategy strategy = new QueryExecutionStrategy(List.<DatabaseOperation>of(
                new DmlSelectOperationExecutor(actions), new DmlUpdateOperationExecutor(actions)));
        JdbcQueryExecutor executor = new JdbcQueryExecutor(strategy, graphReader);

        SQLBuilder sqlBuilder = new SQLBuilder(variables);
        InputMapperImpl inputMapper = new InputMapperImpl(sqlBuilder, variables);
        SourceMapper sourceMapper = new SourceMapper();

        DaoMethodContext daoMethods = new DaoMethodContext();
        OpenMapperSQLContext sqlContext = new OpenMapperSQLContext(
                new DaoMethodFacade(daoMethods, sourceMapper));

        ModelMetadataContext models = new ModelMetadataContext();
        models.registerModel(Item.class, new ModelSpecificationReader().readModelMetadata(Item.class));
        mappingPlans.planFor(Item.class);

        ModelFieldReader fieldReader = new ModelFieldReader();
        ModelExtractor modelExtractor =
                new ModelExtractor(new FieldValueGenerator(), models, null, fieldReader, columnTypes);
        ModelPropertyExtractor propertyExtractor = new ModelPropertyExtractor(modelExtractor,
                new EntityWriteListeners(List.of()), new EntityIdGenerators(List.of(), fieldReader));

        for (Method method : ItemDao.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Sql.class)) {
                daoMethods.registerMethod(method);
            }
        }
        sqlContext.updateContext("findAll", sourceMapper.map("select * from standalone_item"));
        sqlContext.updateContext("findById", sourceMapper.map("select * from standalone_item where id = [id]"));

        AssociationCascade cascade = new AssociationCascade(models, sqlContext, modelExtractor, fieldReader,
                inputMapper, executor, new EntityWriteListeners(List.of()),
                new EntityIdGenerators(List.of(), fieldReader));

        QueryFacadeImpl facade = new QueryFacadeImpl(sqlContext, inputMapper, executor, propertyExtractor, variables,
                new OpenMapperCachingFacade(propertyExtractor, variables, inputMapper, new QueryCacheImpl()),
                new NotFoundTranslator(), new DaoMethodPlanRegistry(handlers), handlers, cascade, fieldReader, models);

        return (ItemDao) new InvocationProxy()
                .makeProxyFor(new QueryMappingInvocationHandler(facade, dataSource), ItemDao.class.getName());
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
        List<Item> findAll();

        @DaoMethod(operation = DmlOperation.SELECT)
        Item findById(@Param(name = "id") Long id);
    }
}
