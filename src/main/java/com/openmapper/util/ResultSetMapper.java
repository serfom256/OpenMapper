package com.openmapper.util;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetMapper {

    public static Object mapResult(ResultSet resultSet) throws SQLException {
        return resultSet.getObject(1);
    }
}
