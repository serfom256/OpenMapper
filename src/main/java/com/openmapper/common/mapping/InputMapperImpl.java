package com.openmapper.common.mapping;

import com.openmapper.core.entity.SQLRecord;
import com.openmapper.common.SQLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputMapperImpl implements InputMapper {

    private final SQLBuilder sqlBuilder;
    private final boolean isLogging;

    private static final Logger logger = LoggerFactory.getLogger(InputMapperImpl.class);

    public InputMapperImpl(final boolean isLogging) {
        this.sqlBuilder = new SQLBuilder();
        this.isLogging = isLogging;
    }

    @Override
    public String mapSql(SQLRecord entity, Map<String, Object> toReplace) {
        String result = sqlBuilder.buildSql(entity, toReplace);
        if (isLogging) logger.info(result);
        return result;
    }
}
