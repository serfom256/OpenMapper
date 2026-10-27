package io.github.serfom256.core.query.facade;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.query.executors.QueryExecutor;
import io.github.serfom256.core.query.common.NotFoundTranslator;
import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import java.lang.reflect.Field;

import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.core.query.method.ParameterBinding;
import io.github.serfom256.model.page.Page;
import io.github.serfom256.model.page.PageRequest;
import io.github.serfom256.core.query.method.DaoMethodPlanRegistry;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QueryParameters;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.exceptions.internal.OptimisticLockException;
import io.github.serfom256.parser.model.SQLRecord;

public class QueryFacadeImpl implements QueryFacade {

    private final OpenMapperSQLContext context;
    private final InputMapper mapper;
    private final QueryExecutor queryExecutor;
    private final ModelPropertyExtractor modelPropertyExtractor;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final OpenMapperCachingFacade queryCacheFacade;
    private final NotFoundTranslator notFoundTranslator;
    private final DaoMethodPlanRegistry planRegistry;
    private final ResultSetHandler<?> countHandler;
    private final AssociationCascade associationCascade;
    private final ModelFieldReader fieldReader;
    private final ModelMetadataContext modelMetadataContext;

    private static final Logger logger = LoggerFactory.getLogger(QueryFacadeImpl.class);

    public QueryFacadeImpl(
            OpenMapperSQLContext context,
            InputMapper mapper,
            QueryExecutor queryExecutor,
            ModelPropertyExtractor modelPropertyExtractor,
            OpenMapperGlobalEnvironmentVariables variables,
            OpenMapperCachingFacade queryCacheFacade,
            NotFoundTranslator notFoundTranslator,
            DaoMethodPlanRegistry planRegistry,
            ResultSetHandlerStrategy handlerStrategy,
            AssociationCascade associationCascade,
            ModelFieldReader fieldReader,
            ModelMetadataContext modelMetadataContext) {
        this.context = context;
        this.mapper = mapper;
        this.queryExecutor = queryExecutor;
        this.modelPropertyExtractor = modelPropertyExtractor;
        this.variables = variables;
        this.queryCacheFacade = queryCacheFacade;
        this.notFoundTranslator = notFoundTranslator;
        this.planRegistry = planRegistry;
        this.countHandler = handlerStrategy.getHandlerByMethodReturnType(Long.class);
        this.associationCascade = associationCascade;
        this.fieldReader = fieldReader;
        this.modelMetadataContext = modelMetadataContext;
    }

    @Override
    public Object invokeMethodQueryWithParameters(Method method, Object[] args, DataSource dataSource) {
        final DaoMethodPlan plan = planRegistry.planFor(method);
        SQLRecord sql = context.getSqlProcedure(plan, args);

        if (plan.isBatch()) {
            return executeBatch(plan, args, sql, dataSource);
        }
        if (plan.returnsPage()) {
            return executePage(plan, method, args, sql, dataSource);
        }

        final QueryParameters queryParameters = queryCacheFacade.getQuery(plan, method, args, sql);
        final BoundSql boundSql = queryParameters.getBoundSql();
        QuerySpecifications querySpecifications = queryParameters.getQuerySpecifications();

        querySpecifications.setDataSource(dataSource);

        return executeDaoMethod(boundSql, plan, method, args, querySpecifications);
    }

    private void cascadeAssociations(DaoMethodPlan plan, Object[] args,
                                     QuerySpecifications specifications, Object result) {
        Object model = modelArgument(plan, args);
        if (model == null || !associationCascade.hasCascades(model)) {
            return;
        }
        Object key = parentKey(model, result);
        if (key != null) {
            associationCascade.cascade(model, key, specifications.getDataSource());
        }
    }

    private Object modelArgument(DaoMethodPlan plan, Object[] args) {
        if (args == null) {
            return null;
        }
        ParameterBinding[] bindings = plan.parameters();
        for (int i = 0; i < bindings.length && i < args.length; i++) {
            if (bindings[i].kind() == ParameterBinding.Kind.MODEL) {
                return args[i];
            }
        }
        return null;
    }

    private Object parentKey(Object model, Object result) {
        Field primaryKey = primaryKeyField(model.getClass());
        if (primaryKey != null) {
            Object declared = fieldReader.read(model, primaryKey);
            if (isPresent(declared)) {
                return declared;
            }
        }
        return generatedKey(result);
    }

    private Field primaryKeyField(Class<?> type) {
        Model model = type.getAnnotation(Model.class);
        if (model == null) {
            return null;
        }
        try {
            return type.getDeclaredField(model.primaryKey());
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    private boolean isPresent(Object value) {
        if (value == null) {
            return false;
        }
        return !(value instanceof Number number) || number.longValue() != 0L;
    }

    private Object generatedKey(Object result) {
        if (result instanceof Number) {
            return result;
        }
        if (result instanceof Iterable<?> iterable) {
            for (Object first : iterable) {
                return first;
            }
        }
        return null;
    }

    private Object executePage(DaoMethodPlan plan, Method method, Object[] args, SQLRecord sql,
                               DataSource dataSource) {
        PageRequest pageRequest = pageRequestOf(plan, args);

        QuerySpecifications contentSpecifications = new QuerySpecifications();
        modelPropertyExtractor.extractQuerySpecifications(plan, args, contentSpecifications);
        contentSpecifications.setDataSource(dataSource);
        BoundSql contentSql = mapper.mapSql(sql, contentSpecifications.getParams());

        Object content = queryExecutor.execute(contentSql, contentSpecifications);
        List<?> rows = content instanceof List<?> list ? list : List.of();

        return new Page<>(rows, pageRequest.page(), pageRequest.size(),
                count(plan, contentSpecifications, dataSource));
    }

    private long count(DaoMethodPlan plan, QuerySpecifications contentSpecifications,
                       DataSource dataSource) {
        SQLRecord countRecord = context.getSqlProcedure(plan.countProcedure());
        QuerySpecifications countSpecifications = new QuerySpecifications();
        countSpecifications.setDataSource(dataSource);
        countSpecifications.setOperation(plan.operation());
        countSpecifications.setReturnType(Long.class);
        countSpecifications.setHandler(countHandler);

        BoundSql countSql = mapper.mapSql(countRecord, contentSpecifications.getParams());
        Object total = queryExecutor.execute(countSql, countSpecifications);
        return total instanceof Number number ? number.longValue() : 0L;
    }

    private PageRequest pageRequestOf(DaoMethodPlan plan, Object[] args) {
        ParameterBinding[] bindings = plan.parameters();
        for (int i = 0; i < bindings.length; i++) {
            if (bindings[i].kind() == ParameterBinding.Kind.PAGE_REQUEST) {
                return (PageRequest) args[i];
            }
        }
        throw new IllegalStateException("no PageRequest parameter on " + plan.procedure());
    }

    private Object executeBatch(DaoMethodPlan plan, Object[] args, SQLRecord sql, DataSource dataSource) {
        Collection<?> elements = (Collection<?>) args[plan.batchParameterIndex()];
        if (elements == null || elements.isEmpty()) {
            return 0;
        }

        List<BoundSql> statements = new ArrayList<>(elements.size());
        boolean hasOptimisticLock = false;
        for (Object element : elements) {
            QuerySpecifications elementSpecifications = new QuerySpecifications();
            modelPropertyExtractor.extractBatchElement(plan, element, elementSpecifications);
            statements.add(mapper.mapSql(sql, elementSpecifications.getParams()));
            hasOptimisticLock |= elementSpecifications.hasOptimisticLock();
        }

        QuerySpecifications specifications = new QuerySpecifications();
        specifications.setHandler(plan.handler());
        specifications.setOperation(plan.operation());
        specifications.setReturnType(plan.genericReturnType());
        specifications.setShouldReturnGeneratedKeys(plan.returnGeneratedKeys());
        specifications.setGeneratedKeyColumns(plan.generatedKeyColumns());
        specifications.setFetchSize(plan.fetchSize());
        specifications.setQueryTimeoutSeconds(plan.queryTimeoutSeconds());
        specifications.setHasOptimisticLock(hasOptimisticLock);
        specifications.setDataSource(dataSource);

        return queryExecutor.executeBatch(statements, specifications);
    }

    private Object executeDaoMethod(
            BoundSql boundSql,
            DaoMethodPlan plan,
            Method method,
            Object[] args,
            QuerySpecifications querySpecifications) {
        Object result = queryExecutor.execute(boundSql, querySpecifications);

        if (plan.returnsStream()) {
            return result;
        }
        if (plan.returnsOptional()) {
            return Optional.ofNullable(result);
        }
        notFoundTranslator.translateIfAbsent(plan.notFoundException(), method, result);
        cascadeAssociations(plan, args, querySpecifications, result);
        return result;
    }
}
