package com.openmapper.common.reflect;

import com.openmapper.annotations.Param;
import com.openmapper.annotations.entity.Dto;
import com.openmapper.annotations.entity.Model;
import com.openmapper.core.query.model.QuerySpecifications;
import com.openmapper.core.query.common.AdditionalSpecificationExtractor;
import com.openmapper.core.query.model.ModelSpecifications;
import com.openmapper.mappers.ModelExtractor;

import static com.openmapper.model.operations.DmlOperation.UPDATE;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ModelPropertyExtractor {

    private final ModelExtractor modelExtractor;
    private final AdditionalSpecificationExtractor specificationExtractor;

    public ModelPropertyExtractor(ModelExtractor modelExtractor,
            AdditionalSpecificationExtractor specificationExtractor) {
        this.modelExtractor = modelExtractor;
        this.specificationExtractor = specificationExtractor;
    }

    public void extractQuerySpecifications(Method method, Object[] args, QuerySpecifications querySpecifications) {
        final Parameter[] methodParams = method.getParameters();
        final Map<String, Object> params = new HashMap<>();

        if (querySpecifications.getParams() == null) { // prevent multiple call on retry
            specificationExtractor.extractAdditionalQuerySpecifications(method, querySpecifications);
        }

        for (int i = 0; i < methodParams.length; i++) {
            final Parameter parameter = methodParams[i];
            if (parameter.getType().getAnnotation(Model.class) != null) {
                ModelSpecifications specifications = modelExtractor.extractSpecification(args[i], querySpecifications);

                params.putAll(specifications.getParams());
                querySpecifications.setPreviousOptimisticLockValue(specifications.getGeneratedOptimisticLockValue());
                querySpecifications.setHasOptimisticLock(specifications.getOptimisticLockName() != null);

            } else if (parameter.getType().getAnnotation(Dto.class) != null) {
                ModelSpecifications specifications = modelExtractor.extractSpecification(args[i], querySpecifications);
                params.putAll(specifications.getParams());
                
            } else {
                final Param annotation = parameter.getAnnotation(Param.class);
                params.put(annotation == null ? parameter.getName() : annotation.name(), args[i]);
            }
        }
        if (querySpecifications.getOperation() != UPDATE) {
            querySpecifications.setHasOptimisticLock(false);
        }

        querySpecifications.setParams(params);
    }
}
