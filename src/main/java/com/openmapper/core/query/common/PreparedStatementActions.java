package com.openmapper.core.query.common;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import com.openmapper.core.query.handlers.ResultSetHandler;
import com.openmapper.exceptions.internal.OptimisticLockException;

@Component
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

    public <T> Object executeUpdate(
            ResultSetHandler<T> handler,
            Type returnType,
            PreparedStatement ps,
            boolean returnPrimaryKeys,
            boolean hasOptimisticLock) throws SQLException {

        int updatedCount = ps.executeUpdate();
        if (returnType == void.class || returnType == Void.class) {
            return Void.TYPE;
        }

        if (hasOptimisticLock && updatedCount == 0) {
            throw new OptimisticLockException();
        }

        if (!returnPrimaryKeys) {
            return updatedCount;
        }

        ResultSet rs = ps.getGeneratedKeys();
        final Object queryResult = handler.handle(rs, returnType);
        rs.close();
        return queryResult;
    }
}
