package io.github.serfom256.core.query.facade;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import io.github.serfom256.annotations.entity.Joined;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.context.model.ModelMetadata;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.query.executors.QueryExecutor;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.model.operations.DmlOperation;
import io.github.serfom256.parser.model.SQLRecord;

public class AssociationCascade {

    private final ModelMetadataContext modelMetadataContext;
    private final OpenMapperSQLContext sqlContext;
    private final ModelExtractor modelExtractor;
    private final ModelFieldReader fieldReader;
    private final InputMapper mapper;
    private final QueryExecutor queryExecutor;
    private final EntityWriteListeners writeListeners;
    private final EntityIdGenerators idGenerators;

    public AssociationCascade(ModelMetadataContext modelMetadataContext,
                              OpenMapperSQLContext sqlContext,
                              ModelExtractor modelExtractor,
                              ModelFieldReader fieldReader,
                              InputMapper mapper,
                              QueryExecutor queryExecutor,
                              EntityWriteListeners writeListeners,
                              EntityIdGenerators idGenerators) {
        this.modelMetadataContext = modelMetadataContext;
        this.sqlContext = sqlContext;
        this.modelExtractor = modelExtractor;
        this.fieldReader = fieldReader;
        this.mapper = mapper;
        this.queryExecutor = queryExecutor;
        this.writeListeners = writeListeners;
        this.idGenerators = idGenerators;
    }

    public boolean hasCascades(Object model) {
        if (model == null) {
            return false;
        }
        ModelMetadata metadata = modelMetadataContext.getMetadataByType(model.getClass());
        return metadata != null && !metadata.getCascadingAssociations().isEmpty();
    }

    public void cascade(Object model, Object parentKey, DataSource dataSource) {
        ModelMetadata metadata = modelMetadataContext.getMetadataByType(model.getClass());
        for (Map.Entry<Field, Joined> association : metadata.getCascadingAssociations().entrySet()) {
            Collection<?> elements = elementsOf(model, association.getKey());
            if (elements.isEmpty()) {
                continue;
            }
            saveElements(elements, association.getValue(), parentKey, dataSource);
        }
    }

    private Collection<?> elementsOf(Object model, Field field) {
        Object value = fieldReader.read(model, field);
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return collection;
        }
        return List.of(value);
    }

    private String foreignKeyParameter(Class<?> elementType, Joined joined) {
        try {
            Field field = elementType.getDeclaredField(joined.to());
            io.github.serfom256.annotations.entity.Field column =
                    field.getAnnotation(io.github.serfom256.annotations.entity.Field.class);
            return column == null || column.name().isEmpty() ? field.getName() : column.name();
        } catch (NoSuchFieldException e) {
            return joined.to();
        }
    }

    private void saveElements(Collection<?> elements, Joined joined, Object parentKey,
                              DataSource dataSource) {
        SQLRecord procedure = sqlContext.getSqlProcedure(joined.saveProcedure());
        List<BoundSql> statements = new ArrayList<>(elements.size());

        for (Object element : elements) {
            idGenerators.generateId(element, DmlOperation.INSERT);
            writeListeners.beforeWrite(element, DmlOperation.INSERT);
            QuerySpecifications elementSpecifications = new QuerySpecifications();
            elementSpecifications.setOperation(DmlOperation.INSERT);
            Map<String, Object> params =
                    new java.util.HashMap<>(modelExtractor.extractSpecification(element, elementSpecifications)
                            .getParams());
            params.put(foreignKeyParameter(element.getClass(), joined), parentKey);
            statements.add(mapper.mapSql(procedure, params));
        }

        QuerySpecifications specifications = new QuerySpecifications();
        specifications.setOperation(DmlOperation.INSERT);
        specifications.setDataSource(dataSource);
        specifications.setReturnType(void.class);
        queryExecutor.executeBatch(statements, specifications);
    }
}
