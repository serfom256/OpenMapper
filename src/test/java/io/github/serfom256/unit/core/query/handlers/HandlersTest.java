package io.github.serfom256.unit.core.query.handlers;

import io.github.serfom256.core.query.handlers.*;
import io.github.serfom256.exceptions.common.UnsupportedTypeException;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HandlersTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class ResultSetHandlerStrategyTest {

        @Mock ResultSetObjectMapper objectMapper;
        @Mock ResultSetPrimitiveMapper primitiveMapper;

        @Test
        void getHandler_primitiveTypes() {
            EntityResultSetHandler entityHandler = new EntityResultSetHandler(objectMapper, primitiveMapper);
            IterableResultSetHandler iterableHandler = new IterableResultSetHandler(objectMapper, primitiveMapper);
            EmptyResultSetHandler emptyHandler = new EmptyResultSetHandler();

            ResultSetHandlerStrategy strategy = new ResultSetHandlerStrategy(entityHandler, emptyHandler, iterableHandler);

            assertNotNull(strategy.getHandlerByMethodReturnType(String.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Integer.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(int.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Long.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(long.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Double.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(double.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Boolean.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(boolean.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Short.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(short.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Byte.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(byte.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Character.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(char.class));
        }

        @Test
        void getHandler_voidType() {
            EntityResultSetHandler entityHandler = new EntityResultSetHandler(objectMapper, primitiveMapper);
            IterableResultSetHandler iterableHandler = new IterableResultSetHandler(objectMapper, primitiveMapper);
            EmptyResultSetHandler emptyHandler = new EmptyResultSetHandler();
            ResultSetHandlerStrategy strategy = new ResultSetHandlerStrategy(entityHandler, emptyHandler, iterableHandler);

            assertNotNull(strategy.getHandlerByMethodReturnType(void.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Void.class));
        }

        @Test
        void getHandler_iterableTypes() {
            EntityResultSetHandler entityHandler = new EntityResultSetHandler(objectMapper, primitiveMapper);
            IterableResultSetHandler iterableHandler = new IterableResultSetHandler(objectMapper, primitiveMapper);
            EmptyResultSetHandler emptyHandler = new EmptyResultSetHandler();
            ResultSetHandlerStrategy strategy = new ResultSetHandlerStrategy(entityHandler, emptyHandler, iterableHandler);

            assertNotNull(strategy.getHandlerByMethodReturnType(List.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(ArrayList.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Collection.class));
            assertNotNull(strategy.getHandlerByMethodReturnType(Iterable.class));
        }

        @Test
        void getHandler_unsupportedType_throws() {
            EntityResultSetHandler entityHandler = new EntityResultSetHandler(objectMapper, primitiveMapper);
            IterableResultSetHandler iterableHandler = new IterableResultSetHandler(objectMapper, primitiveMapper);
            EmptyResultSetHandler emptyHandler = new EmptyResultSetHandler();
            ResultSetHandlerStrategy strategy = new ResultSetHandlerStrategy(entityHandler, emptyHandler, iterableHandler);

            assertThrows(UnsupportedTypeException.class,
                    () -> strategy.getHandlerByMethodReturnType(Map.class));
        }
    }

    @Nested
    class EmptyResultSetHandlerTest {

        @Test
        void handle_returnsNull() {
            EmptyResultSetHandler handler = new EmptyResultSetHandler();
            assertNull(handler.handle(null, Void.class));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class EntityResultSetHandlerTest {

        @Mock ResultSetObjectMapper objectMapper;
        @Mock ResultSetPrimitiveMapper primitiveMapper;
        @Mock ResultSet resultSet;

        @Test
        void handle_primitiveType_delegatesToPrimitiveMapper() throws SQLException {
            when(primitiveMapper.extract(any(), any(), any(), any())).thenReturn(List.of("result"));
            EntityResultSetHandler handler = new EntityResultSetHandler(objectMapper, primitiveMapper);

            Object result = handler.handle(resultSet, String.class);
            assertEquals("result", result);
        }

        @Test
        void handle_primitiveType_emptyResult_returnsNull() throws SQLException {
            when(primitiveMapper.extract(any(), any(), any(), any())).thenReturn(List.of());
            EntityResultSetHandler handler = new EntityResultSetHandler(objectMapper, primitiveMapper);

            Object result = handler.handle(resultSet, Integer.class);
            assertNull(result);
        }
    }
}
