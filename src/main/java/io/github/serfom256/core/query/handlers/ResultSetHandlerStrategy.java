package io.github.serfom256.core.query.handlers;

import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.exceptions.common.UnsupportedTypeException;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResultSetHandlerStrategy {

    private final Map<Class<?>, ResultSetHandler<?>> strategy = new HashMap<>(64);

    private final EntityResultSetHandler entityResultSetHandler;

    public ResultSetHandlerStrategy(
            EntityResultSetHandler entityResultSetHandler,
            EmptyResultSetHandler emptyResultSetHandler,
            IterableResultSetHandler iterableResultSetHandler) {
        this.entityResultSetHandler = entityResultSetHandler;

        strategy.put(String.class, entityResultSetHandler);
        strategy.put(Number.class, entityResultSetHandler);

        strategy.put(Integer.class, entityResultSetHandler);
        strategy.put(int.class, entityResultSetHandler);

        strategy.put(Double.class, entityResultSetHandler);
        strategy.put(double.class, entityResultSetHandler);

        strategy.put(Float.class, entityResultSetHandler);
        strategy.put(float.class, entityResultSetHandler);

        strategy.put(Long.class, entityResultSetHandler);
        strategy.put(long.class, entityResultSetHandler);

        strategy.put(Short.class, entityResultSetHandler);
        strategy.put(short.class, entityResultSetHandler);

        strategy.put(Byte.class, entityResultSetHandler);
        strategy.put(byte.class, entityResultSetHandler);

        strategy.put(Character.class, entityResultSetHandler);
        strategy.put(char.class, entityResultSetHandler);

        strategy.put(Boolean.class, entityResultSetHandler);
        strategy.put(boolean.class, entityResultSetHandler);

        strategy.put(Void.class, emptyResultSetHandler);
        strategy.put(void.class, emptyResultSetHandler);

        strategy.put(Iterable.class, iterableResultSetHandler);
        strategy.put(List.class, iterableResultSetHandler);
        strategy.put(AbstractList.class, iterableResultSetHandler);
        strategy.put(ArrayList.class, iterableResultSetHandler);
        strategy.put(LinkedList.class, iterableResultSetHandler);
        strategy.put(Collection.class, iterableResultSetHandler);
    }

    public ResultSetHandler<?> getHandlerByMethodReturnType(Class<?> returnType) {
        if (returnType.getAnnotation(Model.class) != null) {
            return entityResultSetHandler;
        }
        ResultSetHandler<?> handler = strategy.get(returnType);
        if (handler == null) {
            throw new UnsupportedTypeException(returnType.getTypeName());
        }
        return handler;
    }
}
