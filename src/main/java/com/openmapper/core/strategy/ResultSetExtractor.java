package com.openmapper.core.strategy;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetExtractor {
    Object extract(ResultSet resultSet, String name) throws SQLException;
}
