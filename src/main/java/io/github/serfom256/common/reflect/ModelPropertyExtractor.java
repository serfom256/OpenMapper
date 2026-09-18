package io.github.serfom256.common.reflect;

import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.query.method.DaoMethodPlan;
import io.github.serfom256.core.query.method.ParameterBinding;
import io.github.serfom256.core.query.model.QuerySpecifications;
import io.github.serfom256.model.page.PageRequest;
import io.github.serfom256.core.query.model.ModelSpecifications;
import io.github.serfom256.mappers.ModelExtractor;


import java.util.HashMap;
import java.util.Map;

public class ModelPropertyExtractor {

    private final ModelExtractor modelExtractor;
    private final EntityWriteListeners writeListeners;
    private final EntityIdGenerators idGenerators;

    public ModelPropertyExtractor(ModelExtractor modelExtractor, EntityWriteListeners writeListeners,
                                  EntityIdGenerators idGenerators) {
        this.modelExtractor = modelExtractor;
        this.writeListeners = writeListeners;
        this.idGenerators = idGenerators;
    }

    public void extractQuerySpecifications(DaoMethodPlan plan, Object[] args,
                                           QuerySpecifications querySpecifications) {
        extract(plan, plan.parameters(), args, querySpecifications);
    }

    public void extractBatchElement(DaoMethodPlan plan, Object element,
                                    QuerySpecifications querySpecifications) {
        extract(plan, new ParameterBinding[]{plan.batchElement()}, new Object[]{element},
                querySpecifications);
    }

    private void extract(DaoMethodPlan plan, ParameterBinding[] bindings, Object[] args,
                         QuerySpecifications querySpecifications) {
        final Map<String, Object> params = new HashMap<>();

        querySpecifications.setHandler(plan.handler());
        querySpecifications.setOperation(plan.operation());
        querySpecifications.setReturnType(plan.genericReturnType());
        querySpecifications.setShouldReturnGeneratedKeys(plan.returnGeneratedKeys());
        querySpecifications.setFetchSize(plan.fetchSize());
        querySpecifications.setQueryTimeoutSeconds(plan.queryTimeoutSeconds());
        querySpecifications.setStreaming(plan.returnsStream());

        for (int i = 0; i < bindings.length; i++) {
            final ParameterBinding binding = bindings[i];
            switch (binding.kind()) {
                case MODEL -> {
                    idGenerators.generateId(args[i], plan.operation());
                    writeListeners.beforeWrite(args[i], plan.operation());
                    ModelSpecifications specifications =
                            modelExtractor.extractSpecification(args[i], querySpecifications);
                    params.putAll(specifications.getParams());
                    querySpecifications.setHasOptimisticLock(
                            specifications.getOptimisticLockName() != null);
                }
                case DTO -> {
                    idGenerators.generateId(args[i], plan.operation());
                    writeListeners.beforeWrite(args[i], plan.operation());
                    ModelSpecifications specifications =
                            modelExtractor.extractSpecification(args[i], querySpecifications);
                    params.putAll(specifications.getParams());
                }
                case PAGE_REQUEST -> {
                    PageRequest pageRequest = (PageRequest) args[i];
                    params.put(PageRequest.LIMIT_PARAMETER, pageRequest.limit());
                    params.put(PageRequest.OFFSET_PARAMETER, pageRequest.offset());
                }
                case PARAM -> params.put(binding.name(), args[i]);
            }
        }

        if (!plan.operation().advancesVersion()) {
            querySpecifications.setHasOptimisticLock(false);
        }

        querySpecifications.setParams(params);
    }
}
