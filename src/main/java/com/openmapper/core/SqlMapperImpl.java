package com.openmapper.core;

import com.openmapper.entity.FsqlEntity;
import com.openmapper.util.SqlBuilder;
import com.openmapper.util.SqlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SqlMapperImpl implements SqlMapper {

    private final SqlBuilder sqlBuilder;
    private final boolean isLogging;

    private static final Logger logger = LoggerFactory.getLogger(SqlMapperImpl.class);

    public SqlMapperImpl(final boolean isLogging) {
        this.sqlBuilder = new SqlBuilder();
        this.isLogging = isLogging;
    }

    @Override
    public String mapSql(FsqlEntity entity, Map<String, Object> toReplace) {
        String result = sqlBuilder.buildSql(entity, toReplace);
        if (isLogging) logger.info(result);
        return result;
    }
}
