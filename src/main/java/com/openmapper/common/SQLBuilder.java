package com.openmapper.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.query.source.mapping.InputWrapper;
import com.openmapper.parser.model.SQLRecord;
import com.openmapper.parser.model.SQLToken;

@Component
public class SQLBuilder {

    private final InputWrapper inputWrapper;
    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(SQLBuilder.class);

    private static final String QUERY_TRACING_PATTERN = "%s : [%s]";

    public SQLBuilder(InputWrapper inputWrapper, OpenMapperGlobalEnvironmentVariables variables) {
        this.inputWrapper = inputWrapper;
        this.variables = variables;
    }

    public String buildSql(SQLRecord entity, Map<String, Object> toReplace) {

        final Map<String, SQLToken> tokens = entity.getVariables();
        final Map<Integer, String> replaced = new HashMap<>();

        if (tokens.size() > toReplace.size()) {
            throw new IllegalArgumentException(String.format("Invalid count of arguments given! Expected: %s given: %s",
                    tokens.size(), toReplace.size()));
        }

        for (Map.Entry<String, SQLToken> tokenPair : tokens.entrySet()) {
            final Object value = toReplace.get(tokenPair.getKey());
            if (value != null) {
                final String wrappedValue = inputWrapper.wrapInput(value);
                replaced.put(tokenPair.getValue().getPosition(), wrappedValue);
            } else {
                throw new IllegalArgumentException(String.format("Argument: %s not found!", tokenPair.getKey()));
            }
        }

        final StringBuilder result = new StringBuilder();

        for (SQLToken token : entity.getSql()) {
            final String repl = replaced.get(token.getPosition());
            if (repl != null) {
                result.append(repl);
            } else {
                result.append(token.getData());
            }
        }
        
        traceQueryBeforeSubstitution(entity.getSql(), replaced);
        return result.toString();
    }

    private void traceQueryBeforeSubstitution(List<SQLToken> tokenList, Map<Integer, String> replacedVariables) {
        if (variables.isSqlTracingEnabled()) {
            final StringBuilder queryResult = new StringBuilder();
            final StringBuilder paramsResult = new StringBuilder();

            for (SQLToken token : tokenList) {
                final String repl = replacedVariables.get(token.getPosition());
                if (repl != null) {
                    queryResult.append('?');
                    paramsResult.append(repl+", ");
                } else {
                    queryResult.append(token.getData());
                }
            }
            if(!paramsResult.isEmpty()){
                paramsResult.delete(paramsResult.length()-2, paramsResult.length());
            }

            logger.info(QUERY_TRACING_PATTERN.formatted(queryResult, paramsResult));
        }
    }
}
