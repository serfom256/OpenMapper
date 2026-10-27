package io.github.serfom256;

import io.github.serfom256.cache.QueryCacheImpl;
import io.github.serfom256.common.FileContentReader;
import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.common.reflect.FieldValueGenerator;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.config.OpenMapperGlobalConstants;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.common.DaoMethodFacade;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.environment.PackageScanner;
import io.github.serfom256.core.environment.processors.DaoMethodEnvironmentProcessor;
import io.github.serfom256.core.environment.processors.ModelEnvironmentProcessor;
import io.github.serfom256.core.environment.processors.ProcedureSourceEnvironmentProcessor;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.EntityWriteListener;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.lifecycle.IdGenerator;
import io.github.serfom256.core.proxy.InvocationProxy;
import io.github.serfom256.core.proxy.QueryMappingInvocationHandler;
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
import io.github.serfom256.core.query.source.mapping.impl.InputMapperImpl;
import io.github.serfom256.json.Serializer;
import io.github.serfom256.mappers.ColumnTypeConverter;
import io.github.serfom256.mappers.ColumnTypeRegistry;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.factory.DefaultParserFactory;
import io.github.serfom256.procedures.Procedures;
import io.github.serfom256.procedures.ProceduresBuilder;

import org.jspecify.annotations.Nullable;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point for using openMapper without a Spring application context.
 *
 * <p>Build one instance at startup and keep it for the lifetime of the application; it is
 * thread safe and the DAO proxies it returns are cached.
 *
 * <pre>{@code
 * OpenMapper openMapper = OpenMapper.builder()
 *         .dataSource(dataSource)
 *         .scanPackages("com.acme")
 *         .build();
 *
 * ItemDao items = openMapper.dao(ItemDao.class);
 * }</pre>
 *
 * <p>In a Spring application the starter does this wiring automatically and this class is
 * not needed.
 */
public final class OpenMapper {

    private final QueryFacadeImpl queryFacade;
    private final DataSource dataSource;
    private final Procedures procedures;
    private final InvocationProxy invocationProxy = new InvocationProxy();
    private final Map<Class<?>, Object> daos = new ConcurrentHashMap<>();

    private OpenMapper(QueryFacadeImpl queryFacade, DataSource dataSource, Procedures procedures) {
        this.queryFacade = queryFacade;
        this.dataSource = dataSource;
        this.procedures = procedures;
    }

    /** Starts configuring an instance. A data source and at least one package to scan are required. */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the implementation of a {@code @DaoLayer} interface, creating it on first use.
     *
     * @throws IllegalArgumentException if the type is not an interface
     */
    @SuppressWarnings("unchecked")
    public <T> T dao(Class<T> daoInterface) {
        if (!daoInterface.isInterface()) {
            throw new IllegalArgumentException(
                    "%s is not an interface: openMapper DAOs are interfaces annotated with @DaoLayer"
                            .formatted(daoInterface.getName()));
        }
        return (T) daos.computeIfAbsent(daoInterface, type -> invocationProxy.makeProxyFor(
                new QueryMappingInvocationHandler(queryFacade, dataSource), type.getName()));
    }

    /**
     * Runs statements written at the call site, without a {@code .fsql} file or a
     * {@code @DaoLayer} interface.
     */
    public Procedures procedures() {
        return procedures;
    }

    /** Collects the configuration for an {@link OpenMapper} instance. */
    public static final class Builder {

        private final Map<String, Object> properties = new HashMap<>();
        private final List<ColumnTypeConverter<?>> converters = new ArrayList<>();
        private final List<IdGenerator<?>> idGenerators = new ArrayList<>();
        private final List<EntityWriteListener<?>> writeListeners = new ArrayList<>();

        private @Nullable DataSource dataSource;
        private @Nullable Serializer jsonSerializer;
        private @Nullable ClassLoader classLoader;

        private Builder() {
        }

        /** The data source every DAO will use. Required. */
        public Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        /** Packages scanned for both {@code @DaoLayer} interfaces and {@code @Model} classes. */
        public Builder scanPackages(String... packages) {
            String joined = String.join(",", packages);
            properties.put(OpenMapperGlobalConstants.DAO_PACKAGE_TO_SCAN.value(), joined);
            properties.put(OpenMapperGlobalConstants.MODEL_PACKAGE_TO_SCAN.value(), joined);
            return this;
        }

        /** Packages scanned for {@code @DaoLayer} interfaces only. */
        public Builder daoPackages(String... packages) {
            properties.put(OpenMapperGlobalConstants.DAO_PACKAGE_TO_SCAN.value(), String.join(",", packages));
            return this;
        }

        /** Packages scanned for {@code @Model} classes only. */
        public Builder modelPackages(String... packages) {
            properties.put(OpenMapperGlobalConstants.MODEL_PACKAGE_TO_SCAN.value(), String.join(",", packages));
            return this;
        }

        /**
         * Where to find {@code .fsql} files. Accepts a directory or an explicit file, with or without a
         * {@code classpath:} prefix. Defaults to every {@code .fsql} file on the classpath.
         */
        public Builder sqlLocations(String... locations) {
            properties.put(OpenMapperGlobalConstants.FSQL_FILES_PATH.value(), String.join(",", locations));
            return this;
        }

        /** Enables the query cache. Off by default. */
        public Builder queryCacheEnabled(boolean enabled) {
            properties.put(OpenMapperGlobalConstants.QUERY_CACHE_ENABLED.value(), Boolean.toString(enabled));
            return this;
        }

        /** Enables startup and scanning logs. Off by default. */
        public Builder loggingEnabled(boolean enabled) {
            properties.put(OpenMapperGlobalConstants.LOGGING_ENABLED.value(), Boolean.toString(enabled));
            return this;
        }

        /** Logs each statement as it runs. Off by default. */
        public Builder sqlTracingEnabled(boolean enabled) {
            properties.put(OpenMapperGlobalConstants.SQL_TRACING_ENABLED.value(), Boolean.toString(enabled));
            return this;
        }

        /** Maximum number of values per generated {@code IN} clause. Defaults to 1000. */
        public Builder inClauseChunkSize(int size) {
            properties.put(OpenMapperGlobalConstants.IN_CLAUSE_CHUNK_SIZE.value(), Integer.toString(size));
            return this;
        }

        /** Codec used for {@code @Json} columns. Without one those columns are unsupported. */
        public Builder jsonSerializer(Serializer serializer) {
            this.jsonSerializer = serializer;
            return this;
        }

        /** Registers converters for column types openMapper does not handle natively. */
        public Builder columnTypeConverters(ColumnTypeConverter<?>... columnTypeConverters) {
            this.converters.addAll(Arrays.asList(columnTypeConverters));
            return this;
        }

        /** Registers generators that supply entity ids before insert. */
        public Builder idGenerators(IdGenerator<?>... generators) {
            this.idGenerators.addAll(Arrays.asList(generators));
            return this;
        }

        /** Registers listeners invoked around entity writes. */
        public Builder writeListeners(EntityWriteListener<?>... listeners) {
            this.writeListeners.addAll(Arrays.asList(listeners));
            return this;
        }

        /** Sets any {@code openmapper.*} property directly, for settings without a dedicated method. */
        public Builder property(String name, String value) {
            properties.put(name, value);
            return this;
        }

        /** Class loader used to scan for annotated types and {@code .fsql} files. */
        public Builder classLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        /**
         * Scans the configured packages, loads the {@code .fsql} files and wires the runtime.
         *
         * @throws IllegalStateException if no data source or no package to scan was configured
         */
        public OpenMapper build() {
            if (dataSource == null) {
                throw new IllegalStateException("openMapper needs a data source: call dataSource(...)");
            }
            if (!properties.containsKey(OpenMapperGlobalConstants.DAO_PACKAGE_TO_SCAN.value())
                    && !properties.containsKey(OpenMapperGlobalConstants.MODEL_PACKAGE_TO_SCAN.value())) {
                throw new IllegalStateException(
                        "openMapper needs a package to scan: call scanPackages(...), or daoPackages(...) "
                                + "and modelPackages(...)");
            }

            ClassLoader loader = classLoader != null
                    ? classLoader
                    : Thread.currentThread().getContextClassLoader();

            StandardEnvironment environment = new StandardEnvironment();
            environment.getPropertySources().addFirst(new MapPropertySource("openmapper", properties));
            OpenMapperGlobalEnvironmentVariables variables = new OpenMapperGlobalEnvironmentVariables(environment);

            ColumnTypeRegistry columnTypes = new ColumnTypeRegistry(List.copyOf(converters));
            MappingPlanRegistry mappingPlans = new MappingPlanRegistry(jsonSerializer, columnTypes);
            ResultSetGraphReader graphReader = new ResultSetGraphReader(mappingPlans);
            ResultSetObjectMapper objectMapper = new ResultSetObjectMapper(graphReader);
            ResultSetPrimitiveMapper primitiveMapper = new ResultSetPrimitiveMapper();

            EntityResultSetHandler entityHandler = new EntityResultSetHandler(objectMapper, primitiveMapper);
            IterableResultSetHandler iterableHandler =
                    new IterableResultSetHandler(objectMapper, primitiveMapper);
            ResultSetHandlerStrategy handlers = new ResultSetHandlerStrategy(
                    entityHandler, new EmptyResultSetHandler(), iterableHandler);

            PreparedStatementActions actions = new PreparedStatementActions();
            JdbcQueryExecutor executor = new JdbcQueryExecutor(
                    new QueryExecutionStrategy(List.<DatabaseOperation>of(
                            new DmlSelectOperationExecutor(actions),
                            new DmlUpdateOperationExecutor(actions))),
                    graphReader);

            SQLBuilder sqlBuilder = new SQLBuilder(variables);
            InputMapperImpl inputMapper = new InputMapperImpl(sqlBuilder, variables);
            SourceMapper sourceMapper = new SourceMapper();

            DaoMethodContext daoMethods = new DaoMethodContext();
            ModelMetadataContext models = new ModelMetadataContext();
            OpenMapperSQLContext sqlContext =
                    new OpenMapperSQLContext(new DaoMethodFacade(daoMethods, sourceMapper));

            ModelFieldReader fieldReader = new ModelFieldReader();
            ModelExtractor modelExtractor = new ModelExtractor(
                    new FieldValueGenerator(), models, jsonSerializer, fieldReader, columnTypes);
            EntityWriteListeners listeners = new EntityWriteListeners(List.copyOf(writeListeners));
            EntityIdGenerators generators = new EntityIdGenerators(List.copyOf(idGenerators), fieldReader);
            ModelPropertyExtractor propertyExtractor =
                    new ModelPropertyExtractor(modelExtractor, listeners, generators);

            PackageScanner scanner = new PackageScanner(variables, environment, loader);
            new DaoMethodEnvironmentProcessor(scanner, variables, daoMethods).processEnvironment();
            new ModelEnvironmentProcessor(scanner, variables, new ModelSpecificationReader(), models, mappingPlans)
                    .processEnvironment();
            new ProcedureSourceEnvironmentProcessor(sourceMapper,
                    new FileContentReader(variables, new DefaultParserFactory()),
                    variables, sqlContext, new DefaultResourceLoader(loader)).processEnvironment();

            AssociationCascade cascade = new AssociationCascade(models, sqlContext, modelExtractor, fieldReader,
                    inputMapper, executor, listeners, generators);

            QueryFacadeImpl facade = new QueryFacadeImpl(sqlContext, inputMapper, executor, propertyExtractor,
                    variables,
                    new OpenMapperCachingFacade(propertyExtractor, variables, inputMapper, new QueryCacheImpl()),
                    new NotFoundTranslator(), new DaoMethodPlanRegistry(handlers), handlers, cascade, fieldReader,
                    models);

            DataSource configured = dataSource;
            Procedures procedures = new ProceduresBuilder(sourceMapper, inputMapper, executor,
                    entityHandler, iterableHandler, () -> configured).build();

            return new OpenMapper(facade, dataSource, procedures);
        }
    }
}
