package com.openmapper.core.files.mapping;

import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.files.SqlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputMapperImpl implements InputMapper {

    private final SqlBuilder sqlBuilder;
    private final boolean isLogging;

    private static final Logger logger = LoggerFactory.getLogger(InputMapperImpl.class);

    public InputMapperImpl(final boolean isLogging) {
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
