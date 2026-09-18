package io.github.serfom256.core.query.common;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

import io.github.serfom256.core.query.handlers.ResultSetHandler;
import io.github.serfom256.exceptions.internal.OptimisticLockException;

public class PreparedStatementActions {

    public <T> Object executeQuery(
            ResultSetHandler<T> handler,
            Type returnType,
            PreparedStatement ps) throws SQLException {

        ResultSet rs = ps.executeQuery();
        Object queryResult = handler.handle(rs, returnType);
        rs.close();
        return queryResult;
    }

    public <T> Object executeBatch(
            ResultSetHandler<T> handler,
            Type returnType,
            PreparedStatement ps,
            boolean returnPrimaryKeys,
            boolean hasOptimisticLock) throws SQLException {

        int[] counts = ps.executeBatch();

        if (hasOptimisticLock) {
            int stale = 0;
            for (int count : counts) {
                if (count == 0) {
                    stale++;
                }
            }
            if (stale > 0) {
                throw new OptimisticLockException(
                        "%d of %d batched statements matched no row".formatted(stale, counts.length));
            }
        }

        if (returnPrimaryKeys) {
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return readGeneratedKeys(handler, returnType, rs);
            }
        }

        if (returnType == void.class || returnType == Void.class) {
            return Void.TYPE;
        }

        int affected = 0;
        for (int count : counts) {
            if (count > 0) {
                affected += count;
            } else if (count == Statement.SUCCESS_NO_INFO) {
                affected++;
            }
        }
        return affected;
    }

    public <T> Object executeUpdate(
            ResultSetHandler<T> handler,
            Type returnType,
            PreparedStatement ps,
            boolean returnPrimaryKeys,
            boolean hasOptimisticLock) throws SQLException {

        int updatedCount = ps.executeUpdate();

        if (hasOptimisticLock && updatedCount == 0) {
            throw new OptimisticLockException();
        }

        if (returnType == void.class || returnType == Void.class) {
            return Void.TYPE;
        }

        if (!returnPrimaryKeys) {
            return updatedCount;
        }

        try (ResultSet rs = ps.getGeneratedKeys()) {
            return readGeneratedKeys(handler, returnType, rs);
        }
    }

    private <T> Object readGeneratedKeys(ResultSetHandler<T> handler, Type returnType, ResultSet rs)
            throws SQLException {
        if (!isScalar(returnType)) {
            return handler.handle(rs, returnType);
        }
        List<Object> keys = new ArrayList<>();
        while (rs.next()) {
            keys.add(rs.getObject(1));
        }
        if (keys.isEmpty()) {
            return null;
        }
        return coerce(keys.get(0), (Class<?>) returnType);
    }

    private boolean isScalar(Type returnType) {
        if (!(returnType instanceof Class<?> type)) {
            return false;
        }
        return type.isPrimitive() || Number.class.isAssignableFrom(type) || type == String.class;
    }

    private Object coerce(Object value, Class<?> target) {
        if (!(value instanceof Number number)) {
            return value;
        }
        if (target == long.class || target == Long.class) {
            return number.longValue();
        }
        if (target == int.class || target == Integer.class) {
            return number.intValue();
        }
        if (target == short.class || target == Short.class) {
            return number.shortValue();
        }
        return value;
    }
}
