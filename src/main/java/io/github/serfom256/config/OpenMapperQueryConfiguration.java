package io.github.serfom256.config;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.serfom256.cache.QueryCache;
import io.github.serfom256.cache.QueryCacheImpl;
import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.core.context.OpenMapperSQLContext;

import io.github.serfom256.core.query.common.NotFoundTranslator;
import io.github.serfom256.core.query.common.PreparedStatementActions;
import io.github.serfom256.core.query.executors.JdbcQueryExecutor;
import io.github.serfom256.core.query.executors.QueryExecutionStrategy;
import io.github.serfom256.core.query.executors.QueryExecutor;
import io.github.serfom256.core.query.executors.operations.DatabaseOperation;
import io.github.serfom256.core.query.executors.operations.DmlSelectOperationExecutor;
import io.github.serfom256.core.query.executors.operations.DmlUpdateOperationExecutor;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.procedures.ProceduresBuilder;
import io.github.serfom256.procedures.ProceduresCustomizer;
import io.github.serfom256.procedures.Procedures;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.core.query.facade.AssociationCascade;
import io.github.serfom256.core.query.facade.OpenMapperCachingFacade;
import io.github.serfom256.core.query.facade.QueryFacadeImpl;
import io.github.serfom256.core.query.handlers.EmptyResultSetHandler;
import io.github.serfom256.core.query.handlers.EntityResultSetHandler;
import io.github.serfom256.core.query.handlers.IterableResultSetHandler;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.core.query.method.DaoMethodPlanRegistry;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.core.query.source.mapping.impl.InputMapperImpl;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;

@Configuration(proxyBeanMethods = false)
public class OpenMapperQueryConfiguration {

    @Bean
    public SQLBuilder sqlBuilder(OpenMapperGlobalEnvironmentVariables variables) {
        return new SQLBuilder(variables);
    }

    @Bean
    public InputMapperImpl inputMapperImpl(SQLBuilder sqlBuilder, OpenMapperGlobalEnvironmentVariables variables) {
        return new InputMapperImpl(sqlBuilder, variables);
    }

    @Bean
    public DaoMethodPlanRegistry daoMethodPlanRegistry(ResultSetHandlerStrategy resultSetHandlerStrategy) {
        return new DaoMethodPlanRegistry(resultSetHandlerStrategy);
    }

    @Bean
    public NotFoundTranslator notFoundTranslator() {
        return new NotFoundTranslator();
    }

    @Bean
    public PreparedStatementActions preparedStatementActions() {
        return new PreparedStatementActions();
    }

    @Bean
    public DmlSelectOperationExecutor dmlSelectOperationExecutor(PreparedStatementActions preparedStatementActions) {
        return new DmlSelectOperationExecutor(preparedStatementActions);
    }

    @Bean
    public DmlUpdateOperationExecutor dmlUpdateOperationExecutor(PreparedStatementActions preparedStatementActions) {
        return new DmlUpdateOperationExecutor(preparedStatementActions);
    }

    @Bean
    public QueryExecutionStrategy queryExecutionStrategy(List<DatabaseOperation> databaseOperationList) {
        return new QueryExecutionStrategy(databaseOperationList);
    }

    @Bean
    public JdbcQueryExecutor jdbcQueryExecutor(QueryExecutionStrategy queryExecutionStrategy,
                                              ResultSetGraphReader resultSetGraphReader) {
        return new JdbcQueryExecutor(queryExecutionStrategy, resultSetGraphReader);
    }

    @Bean
    public EmptyResultSetHandler emptyResultSetHandler() {
        return new EmptyResultSetHandler();
    }

    @Bean
    public EntityResultSetHandler entityResultSetHandler(ResultSetObjectMapper objectMapper,
            ResultSetPrimitiveMapper primitiveMapper) {
        return new EntityResultSetHandler(objectMapper, primitiveMapper);
    }

    @Bean
    public IterableResultSetHandler iterableResultSetHandler(ResultSetObjectMapper objectMapper,
            ResultSetPrimitiveMapper primitiveMapper) {
        return new IterableResultSetHandler(objectMapper, primitiveMapper);
    }

    @Bean
    public Procedures procedures(SourceMapper sourceMapper,
                                 InputMapper mapper,
                                 QueryExecutor queryExecutor,
                                 EntityResultSetHandler entityResultSetHandler,
                                 IterableResultSetHandler iterableResultSetHandler,
                                 ObjectProvider<DataSource> dataSource,
                                 ObjectProvider<ProceduresCustomizer> customizers) {
        ProceduresBuilder builder = new ProceduresBuilder(sourceMapper, mapper, queryExecutor,
                entityResultSetHandler, iterableResultSetHandler, dataSource::getObject);
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    @Bean
    public ResultSetHandlerStrategy resultSetHandlerStrategy(EntityResultSetHandler entityResultSetHandler,
            EmptyResultSetHandler emptyResultSetHandler,
            IterableResultSetHandler iterableResultSetHandler) {
        return new ResultSetHandlerStrategy(entityResultSetHandler, emptyResultSetHandler, iterableResultSetHandler);
    }

    @Bean
    public QueryCache queryCacheImpl() {
        return new QueryCacheImpl();
    }

    @Bean
    public OpenMapperCachingFacade openMapperCachingFacade(ModelPropertyExtractor modelPropertyExtractor,
            OpenMapperGlobalEnvironmentVariables variables,
            InputMapper mapper,
            QueryCache queryCache) {
        return new OpenMapperCachingFacade(modelPropertyExtractor, variables, mapper, queryCache);
    }

    @Bean
    public AssociationCascade associationCascade(ModelMetadataContext modelMetadataContext,
                                                 OpenMapperSQLContext sqlContext,
                                                 ModelExtractor modelExtractor,
                                                 ModelFieldReader modelFieldReader,
                                                 InputMapper mapper,
                                                 QueryExecutor queryExecutor,
                                                 EntityWriteListeners entityWriteListeners,
                                                 EntityIdGenerators entityIdGenerators) {
        return new AssociationCascade(modelMetadataContext, sqlContext, modelExtractor, modelFieldReader,
                mapper, queryExecutor, entityWriteListeners, entityIdGenerators);
    }

    @Bean
    public QueryFacadeImpl queryFacadeImpl(OpenMapperSQLContext context,
            InputMapper mapper,
            QueryExecutor queryExecutor,
            ModelPropertyExtractor modelPropertyExtractor,
            OpenMapperGlobalEnvironmentVariables variables,
            OpenMapperCachingFacade queryCacheFacade,
            NotFoundTranslator notFoundTranslator,
            DaoMethodPlanRegistry daoMethodPlanRegistry,
            ResultSetHandlerStrategy resultSetHandlerStrategy,
            AssociationCascade associationCascade,
            ModelFieldReader modelFieldReader,
            ModelMetadataContext modelMetadataContext) {
        return new QueryFacadeImpl(context, mapper, queryExecutor, modelPropertyExtractor, variables,
                queryCacheFacade, notFoundTranslator, daoMethodPlanRegistry, resultSetHandlerStrategy,
                associationCascade, modelFieldReader, modelMetadataContext);
    }
}
