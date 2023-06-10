package com.openmapper.common.mapping;

import com.openmapper.common.entity.SQLRecord;
import com.openmapper.common.SQLBuilder;
import com.openmapper.config.OpenMapperGlobalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputMapperImpl implements InputMapper {

    private final SQLBuilder sqlBuilder;

    private final OpenMapperGlobalContext globalContext;

    private static final Logger logger = LoggerFactory.getLogger(InputMapperImpl.class);

    public InputMapperImpl(OpenMapperGlobalContext globalContext) {
        this.globalContext = globalContext;
        this.sqlBuilder = new SQLBuilder();
    }

    @Override
    public String mapSql(SQLRecord entity, Map<String, Object> toReplace) {
        String result = sqlBuilder.buildSql(entity, toReplace);
        if (globalContext.isLogging()) logger.info(result);
        return result;
    }
}
