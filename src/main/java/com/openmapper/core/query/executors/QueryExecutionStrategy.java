package com.openmapper.core.query.executors;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

import com.openmapper.common.operations.DmlOperation;
import com.openmapper.core.query.executors.operations.DatabaseOperation;

@Component
public class QueryExecutionStrategy {

    private final Map<DmlOperation, DatabaseOperation> databaseOperations = new EnumMap<>(DmlOperation.class);

    public QueryExecutionStrategy(List<DatabaseOperation> databaseOperationList) {
        databaseOperationList.forEach(operation -> operation.getSupportedOperations()
                .forEach(opType -> databaseOperations.put(opType, operation)));
    }

    public DatabaseOperation getOperationByType(DmlOperation operation) {
        DatabaseOperation databaseOperation = databaseOperations.get(operation);
        if (databaseOperation == null) {
            throw new IllegalStateException("DatabaseOperation not found for operation type: %s".formatted(operation));
        }
        return databaseOperation;
    }
}
