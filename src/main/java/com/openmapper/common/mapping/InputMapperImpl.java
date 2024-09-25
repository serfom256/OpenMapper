package com.openmapper.common.mapping;

import com.openmapper.common.entity.SQLRecord;
import com.openmapper.common.SQLBuilder;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InputMapperImpl implements InputMapper {

    private final SQLBuilder sqlBuilder;

    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(InputMapperImpl.class);

    public InputMapperImpl(OpenMapperGlobalEnvironmentVariables variables) {
        this.variables = variables;
        this.sqlBuilder = new SQLBuilder();
    }

    @Override
    public String mapSql(SQLRecord entity, Map<String, Object> toReplace) {
        String result = sqlBuilder.buildSql(entity, toReplace);
        if (variables.isLogging()) {
            logger.info(result);
        }
        return result;
    }
}
