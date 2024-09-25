package com.openmapper.core.query;

import com.openmapper.annotations.entity.Model;
import com.openmapper.core.query.impl.EmptyResultSetHandler;
import com.openmapper.core.query.impl.EntityResultSetHandler;
import com.openmapper.core.query.impl.IterableResultSetHandler;
import com.openmapper.exceptions.common.UnsupportedTypeException;

import org.springframework.stereotype.Component;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class QueryExecutorStrategy {

    private static final Map<Class<?>, Supplier<ResultSetHandler<?>>> STRATEGY = new HashMap<>(64);

    private final EntityResultSetHandler entityResultSetHandler;

    public QueryExecutorStrategy(
            EntityResultSetHandler entityResultSetHandler,
            EmptyResultSetHandler emptyResultSetHandler,
            IterableResultSetHandler iterableResultSetHandler) {
        this.entityResultSetHandler = entityResultSetHandler;

        STRATEGY.put(String.class, () -> entityResultSetHandler);
        STRATEGY.put(Number.class, () -> entityResultSetHandler);

        STRATEGY.put(Integer.class, () -> entityResultSetHandler);
        STRATEGY.put(int.class, () -> entityResultSetHandler);

        STRATEGY.put(Double.class, () -> entityResultSetHandler);
        STRATEGY.put(double.class, () -> entityResultSetHandler);

        STRATEGY.put(Long.class, () -> entityResultSetHandler);
        STRATEGY.put(long.class, () -> entityResultSetHandler);

        STRATEGY.put(Short.class, () -> entityResultSetHandler);
        STRATEGY.put(short.class, () -> entityResultSetHandler);

        STRATEGY.put(Byte.class, () -> entityResultSetHandler);
        STRATEGY.put(byte.class, () -> entityResultSetHandler);

        STRATEGY.put(Character.class, () -> entityResultSetHandler);
        STRATEGY.put(char.class, () -> entityResultSetHandler);

        STRATEGY.put(Boolean.class, () -> entityResultSetHandler);
        STRATEGY.put(boolean.class, () -> entityResultSetHandler);

        STRATEGY.put(Void.class, () -> emptyResultSetHandler);
        STRATEGY.put(void.class, () -> emptyResultSetHandler);

        STRATEGY.put(Iterable.class, () -> iterableResultSetHandler);
        STRATEGY.put(List.class, () -> iterableResultSetHandler);
        STRATEGY.put(AbstractList.class, () -> iterableResultSetHandler);
        STRATEGY.put(ArrayList.class, () -> iterableResultSetHandler);
        STRATEGY.put(LinkedList.class, () -> iterableResultSetHandler);
        STRATEGY.put(Collection.class, () -> iterableResultSetHandler);
    }

    public ResultSetHandler<?> getExecutorByMethodReturnType(Class<?> returnType) {
        Supplier<ResultSetHandler<?>> resultSetHandlerSupplier = STRATEGY.get(returnType);
        if (returnType.getAnnotation(Model.class) != null) {
            return entityResultSetHandler;
        }
        if (resultSetHandlerSupplier == null) {
            throw new UnsupportedTypeException(returnType.getTypeName());
        }
        return resultSetHandlerSupplier.get();
    }
}
