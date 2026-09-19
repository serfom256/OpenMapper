package io.github.serfom256.unit.mappers;

import io.github.serfom256.exceptions.internal.InvalidResultCountException;
import io.github.serfom256.exceptions.internal.ResultSetExtractionException;
import io.github.serfom256.mappers.ResultSetUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultSetUtilsTest {

    @Mock ResultSet resultSet;
    @Mock ResultSetMetaData metaData;

    @Test
    void extractString_byColumnName() throws SQLException {
        when(resultSet.getString("name")).thenReturn("Alice");
        Object result = ResultSetUtils.extractFromResultSet(String.class, resultSet, "name");
        assertEquals("Alice", result);
    }

    @Test
    void extractInt_byColumnName() throws SQLException {
        when(resultSet.getInt("age")).thenReturn(25);
        Object result = ResultSetUtils.extractFromResultSet(int.class, resultSet, "age");
        assertEquals(25, result);
    }

    @Test
    void extractInteger_byColumnName() throws SQLException {
        when(resultSet.getInt("age")).thenReturn(30);
        Object result = ResultSetUtils.extractFromResultSet(Integer.class, resultSet, "age");
        assertEquals(30, result);
    }

    @Test
    void extractLong_byColumnName() throws SQLException {
        when(resultSet.getLong("id")).thenReturn(100L);
        Object result = ResultSetUtils.extractFromResultSet(long.class, resultSet, "id");
        assertEquals(100L, result);
    }

    @Test
    void extractDouble_byColumnName() throws SQLException {
        when(resultSet.getDouble("price")).thenReturn(9.99);
        Object result = ResultSetUtils.extractFromResultSet(double.class, resultSet, "price");
        assertEquals(9.99, result);
    }

    @Test
    void extractBoolean_byColumnName() throws SQLException {
        when(resultSet.getBoolean("active")).thenReturn(true);
        Object result = ResultSetUtils.extractFromResultSet(boolean.class, resultSet, "active");
        assertEquals(true, result);
    }

    @Test
    void extractBigDecimal_byColumnName() throws SQLException {
        when(resultSet.getBigDecimal("amount")).thenReturn(new BigDecimal("123.45"));
        Object result = ResultSetUtils.extractFromResultSet(BigDecimal.class, resultSet, "amount");
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void extractDate_byColumnName() throws SQLException {
        Date date = new Date(System.currentTimeMillis());
        when(resultSet.getDate("created")).thenReturn(date);
        Object result = ResultSetUtils.extractFromResultSet(Date.class, resultSet, "created");
        assertEquals(date, result);
    }

    @Test
    void extractObject_byColumnName() throws SQLException {
        when(resultSet.getObject("data")).thenReturn("something");
        Object result = ResultSetUtils.extractFromResultSet(Object.class, resultSet, "data");
        assertEquals("something", result);
    }

    @Test
    void extractTimestamp_byColumnName() throws SQLException {
        java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
        when(resultSet.getTimestamp("created")).thenReturn(ts);
        Object result = ResultSetUtils.extractFromResultSet(java.sql.Timestamp.class, resultSet, "created");
        assertEquals(ts, result);
    }

    @Test
    void extractUnsupportedType_throwsNPE() {
        assertThrows(NullPointerException.class,
                () -> ResultSetUtils.extractFromResultSet(java.util.UUID.class, resultSet, "col"));
    }

    @Test
    void getFirst_singleColumn() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(resultSet.getInt(1)).thenReturn(42);
        Object result = ResultSetUtils.getFirst(int.class, resultSet);
        assertEquals(42, result);
    }

    @Test
    void getFirst_multipleColumns_throws() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        assertThrows(InvalidResultCountException.class,
                () -> ResultSetUtils.getFirst(int.class, resultSet));
    }

    @Test
    void extractFloat_byColumnName() throws SQLException {
        when(resultSet.getFloat("rating")).thenReturn(4.5f);
        Object result = ResultSetUtils.extractFromResultSet(float.class, resultSet, "rating");
        assertEquals(4.5f, result);
    }

    @Test
    void extractShort_byColumnName() throws SQLException {
        when(resultSet.getShort("level")).thenReturn((short) 3);
        Object result = ResultSetUtils.extractFromResultSet(short.class, resultSet, "level");
        assertEquals((short) 3, result);
    }

    @Test
    void extractByte_byColumnName() throws SQLException {
        when(resultSet.getByte("flags")).thenReturn((byte) 1);
        Object result = ResultSetUtils.extractFromResultSet(byte.class, resultSet, "flags");
        assertEquals((byte) 1, result);
    }

    @Test
    void extractFromResultSet_sqlException_throws() throws SQLException {
        when(resultSet.getString("name")).thenThrow(new SQLException("connection lost"));
        assertThrows(ResultSetExtractionException.class,
                () -> ResultSetUtils.extractFromResultSet(String.class, resultSet, "name"));
    }
}
