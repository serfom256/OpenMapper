package com.openmapper.core.query.source.mapping.impl;

import com.openmapper.common.SQLBuilder;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.query.source.mapping.InputMapper;
import com.openmapper.parser.model.SQLRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InputMapperImpl implements InputMapper {

    private final SQLBuilder sqlBuilder;

    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(InputMapperImpl.class);

    public InputMapperImpl(SQLBuilder sqlBuilder, OpenMapperGlobalEnvironmentVariables variables) {
        this.sqlBuilder = sqlBuilder;
        this.variables = variables;
    }

    @Override
    public String mapSql(SQLRecord entity, Map<String, Object> toReplace) {
        String result = sqlBuilder.buildSql(entity, toReplace);
        if (variables.isSqlQueryTracingEnabled()) {
            logger.info(result);
        }
        return result;
    }
}
