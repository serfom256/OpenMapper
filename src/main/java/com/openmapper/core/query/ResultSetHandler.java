package com.openmapper.core.query;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetHandler<T> {

    T handle(ResultSet rs, Type mappingType) throws SQLException;
}
