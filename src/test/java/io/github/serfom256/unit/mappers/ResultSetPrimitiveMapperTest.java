package io.github.serfom256.unit.mappers;

import io.github.serfom256.exceptions.internal.QueryExecutionError;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultSetPrimitiveMapperTest {

    @Mock ResultSet resultSet;
    @Mock ResultSetMetaData metaData;

    private final ResultSetPrimitiveMapper mapper = new ResultSetPrimitiveMapper();

    @Test
    void extract_multipleRows_returnsList() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(resultSet.getInt(1)).thenReturn(10, 20, 30);

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) mapper.extract(int.class, int.class, int.class, resultSet);

        assertEquals(3, result.size());
        assertEquals(10, result.get(0));
        assertEquals(20, result.get(1));
        assertEquals(30, result.get(2));
    }

    @Test
    void extract_emptyResultSet_returnsEmptyList() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) mapper.extract(String.class, String.class, String.class, resultSet);

        assertTrue(result.isEmpty());
    }

    @Test
    void extract_singleRow_returnsSingleElement() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(resultSet.getString(1)).thenReturn("hello");

        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) mapper.extract(String.class, String.class, String.class, resultSet);

        assertEquals(1, result.size());
        assertEquals("hello", result.get(0));
    }

    @Test
    void extract_sqlException_throws() throws SQLException {
        when(resultSet.next()).thenThrow(new SQLException("closed"));

        assertThrows(QueryExecutionError.class,
                () -> mapper.extract(int.class, int.class, int.class, resultSet));
    }
}
