package io.github.serfom256.unit.core.query.handlers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.serfom256.core.query.handlers.EmptyResultSetHandler;
import io.github.serfom256.core.query.handlers.EntityResultSetHandler;
import io.github.serfom256.core.query.handlers.IterableResultSetHandler;
import io.github.serfom256.core.query.handlers.ResultSetHandlerStrategy;
import io.github.serfom256.exceptions.common.UnsupportedTypeException;

/**
 * The README lists the return types a DAO method may declare. Resolution is an exact
 * map lookup with no fallback, so a type missing from the strategy fails at startup --
 * which is how float came to be documented but unsupported.
 */
class ResultSetHandlerStrategyTest {

    private final ResultSetHandlerStrategy strategy = new ResultSetHandlerStrategy(
            new EntityResultSetHandler(null, null),
            new EmptyResultSetHandler(),
            new IterableResultSetHandler(null, null));

    @ParameterizedTest
    @DisplayName("every primitive and wrapper the README lists resolves to a handler")
    @ValueSource(classes = {
            int.class, Integer.class, long.class, Long.class, double.class, Double.class,
            float.class, Float.class, short.class, Short.class, byte.class, Byte.class,
            char.class, Character.class, boolean.class, Boolean.class, void.class, Void.class,
            String.class, Number.class})
    void scalarReturnTypesResolve(Class<?> type) {
        assertNotNull(strategy.getHandlerByMethodReturnType(type), type.getName());
    }

    @ParameterizedTest
    @DisplayName("every collection the README lists resolves to a handler")
    @ValueSource(classes = {Iterable.class, Collection.class, List.class, ArrayList.class})
    void collectionReturnTypesResolve(Class<?> type) {
        assertNotNull(strategy.getHandlerByMethodReturnType(type));
    }

    @Test
    @DisplayName("an undeclared return type still fails loudly")
    void unknownReturnTypeThrows() {
        assertThrows(UnsupportedTypeException.class,
                () -> strategy.getHandlerByMethodReturnType(StringBuilder.class));
    }

    @Test
    @DisplayName("float is resolvable: it is documented and was previously missing")
    void floatResolves() {
        assertDoesNotThrow(() -> strategy.getHandlerByMethodReturnType(float.class));
        assertDoesNotThrow(() -> strategy.getHandlerByMethodReturnType(Float.class));
    }
}
