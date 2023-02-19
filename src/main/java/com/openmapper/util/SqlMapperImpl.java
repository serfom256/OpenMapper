package com.openmapper.util;

import com.openmapper.entity.FsqlEntity;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class SqlMapperImpl implements SqlMapper {

    private final SqlBuilder sqlBuilder;
    private final boolean isLogging;

    public SqlMapperImpl(final boolean isLogging) {
        this.sqlBuilder = new SqlBuilder();
        this.isLogging = isLogging;
    }

    @Override
    public String mapSql(FsqlEntity entity, Map<String, Object> toReplace) {
        String result = sqlBuilder.buildSql(entity, toReplace);
        if (isLogging) log.info(result);
        return result;
    }
}
