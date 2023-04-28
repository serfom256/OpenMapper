package com.openmapper.core.strategy;

import com.openmapper.util.ResultSetMapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ObjectResultSetHandler implements ResultSetHandler<Object> {
    @Override
    public Object handle(ResultSet rs, Type mappingType) throws SQLException {
        return ResultSetMapper.mapResult(rs);
    }
}
