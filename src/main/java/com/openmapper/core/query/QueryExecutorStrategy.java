package com.openmapper.core.query;

import com.openmapper.core.strategy.ResultSetHandler;
import com.openmapper.core.strategy.impl.EmptyResultSetHandler;
import com.openmapper.core.strategy.impl.EntityResultSetHandler;
import com.openmapper.core.strategy.impl.IterableResultSetHandler;

import java.util.*;
import java.util.function.Supplier;

public class QueryExecutorStrategy {

    private static final Map<Class<?>, Supplier<ResultSetHandler<?>>> STRATEGY = new HashMap<>(32);

    static {
        STRATEGY.put(String.class, EntityResultSetHandler::new);
        STRATEGY.put(Number.class, EntityResultSetHandler::new);

        STRATEGY.put(Integer.class, EntityResultSetHandler::new);
        STRATEGY.put(int.class, EntityResultSetHandler::new);

        STRATEGY.put(Double.class, EntityResultSetHandler::new);
        STRATEGY.put(double.class, EntityResultSetHandler::new);

        STRATEGY.put(Long.class, EntityResultSetHandler::new);
        STRATEGY.put(long.class, EntityResultSetHandler::new);

        STRATEGY.put(Short.class, EntityResultSetHandler::new);
        STRATEGY.put(short.class, EntityResultSetHandler::new);

        STRATEGY.put(Byte.class, EntityResultSetHandler::new);
        STRATEGY.put(byte.class, EntityResultSetHandler::new);

        STRATEGY.put(Character.class, EntityResultSetHandler::new);
        STRATEGY.put(char.class, EntityResultSetHandler::new);

        STRATEGY.put(Boolean.class, EntityResultSetHandler::new);
        STRATEGY.put(boolean.class, EntityResultSetHandler::new);

        STRATEGY.put(Void.class, EmptyResultSetHandler::new);
        STRATEGY.put(void.class, EmptyResultSetHandler::new);

        STRATEGY.put(Iterable.class, IterableResultSetHandler::new);
        STRATEGY.put(List.class, IterableResultSetHandler::new);
        STRATEGY.put(AbstractList.class, IterableResultSetHandler::new);
        STRATEGY.put(ArrayList.class, IterableResultSetHandler::new);
        STRATEGY.put(LinkedList.class, IterableResultSetHandler::new);
        STRATEGY.put(Collection.class, IterableResultSetHandler::new);
    }

    public ResultSetHandler<?> getExecutorByMethodReturnType(Class<?> returnType) {
        if (returnType.getTypeName().equals("void")) {
            return new EmptyResultSetHandler();
        }
        return STRATEGY.getOrDefault(returnType, EntityResultSetHandler::new).get();
    }
}
