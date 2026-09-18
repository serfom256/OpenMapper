package io.github.serfom256.core.query.source.mapping.impl;

import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.source.mapping.InputMapper;
import io.github.serfom256.parser.model.SQLRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class InputMapperImpl implements InputMapper {

    private final SQLBuilder sqlBuilder;

    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(InputMapperImpl.class);

    public InputMapperImpl(SQLBuilder sqlBuilder, OpenMapperGlobalEnvironmentVariables variables) {
        this.sqlBuilder = sqlBuilder;
        this.variables = variables;
    }

    @Override
    public BoundSql mapSql(SQLRecord entity, Map<String, Object> toReplace) {
        BoundSql boundSql = sqlBuilder.buildSql(entity, toReplace);
        if (variables.isSqlQueryTracingEnabled()) {
            logger.info(boundSql.getSql());
        }
        return boundSql;
    }
}
