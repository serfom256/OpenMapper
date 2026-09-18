package io.github.serfom256.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.parser.model.SQLRecord;
import io.github.serfom256.parser.model.SQLToken;

/**
 * Builds a parameterised statement from a parsed SQL record: every {@code [name]}
 * placeholder becomes a {@code ?} and its value is collected as a bind parameter,
 * in placeholder order. Values are never concatenated into the SQL text, so the
 * statement is immune to SQL injection and reusable by the database's plan cache.
 *
 * <p>A collection expands to one placeholder per element. Inside an {@code IN} an
 * empty collection becomes {@code 1 = 0} and inside a {@code NOT IN} it becomes
 * {@code 1 = 1}, because {@code x NOT IN (NULL)} is UNKNOWN for every row and would
 * silently drop the whole result set. Past
 * {@code openmapper.in.clause.chunk.size} elements the clause is split across
 * several {@code IN}s so a long list cannot hit the database's limit.
 */
public class SQLBuilder {

    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(SQLBuilder.class);

    private static final String QUERY_TRACING_PATTERN = "%s : %s";

    public SQLBuilder(OpenMapperGlobalEnvironmentVariables variables) {
        this.variables = variables;
    }

    private boolean appendParameter(StringBuilder sql, List<Object> parameters, Object value) {
        Collection<?> values = asCollection(value);
        if (values == null) {
            sql.append('?');
            parameters.add(value);
            return false;
        }
        InClause inClause = InClause.endingAt(sql);
        if (values.isEmpty()) {
            return appendEmpty(sql, inClause);
        }
        appendElements(sql, parameters, values, inClause);
        return false;
    }

    private boolean appendEmpty(StringBuilder sql, InClause inClause) {
        if (inClause == null) {
            sql.append("NULL");
            return false;
        }
        sql.setLength(inClause.operandStart());
        sql.append(inClause.emptyPredicate());
        return true;
    }

    private void appendElements(StringBuilder sql, List<Object> parameters, Collection<?> values,
                                InClause inClause) {
        int chunkSize = chunkSize(values.size(), inClause);
        int index = 0;
        for (Object element : values) {
            if (index > 0) {
                sql.append(index % chunkSize == 0 ? inClause.chunkSeparator() : ", ");
            }
            sql.append('?');
            parameters.add(element);
            index++;
        }
    }

    private int chunkSize(int size, InClause inClause) {
        int configured = variables.getInClauseChunkSize();
        if (inClause == null || configured <= 0 || size <= configured) {
            return Integer.MAX_VALUE;
        }
        return configured;
    }

    private Collection<?> asCollection(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection;
        }
        if (value instanceof Object[] array) {
            return Arrays.asList(array);
        }
        return null;
    }

    public BoundSql buildSql(SQLRecord entity, Map<String, Object> toReplace) {

        final Map<String, SQLToken> tokens = entity.getVariables();

        if (tokens.size() > toReplace.size()) {
            throw new IllegalArgumentException(String.format("Invalid count of arguments given! Expected: %s given: %s",
                    tokens.size(), toReplace.size()));
        }

        final Map<Integer, String> variableByPosition = entity.getVariablePositions();

        final StringBuilder sql = new StringBuilder();
        final List<Object> parameters = new ArrayList<>(tokens.size());

        boolean dropCloseParen = false;

        for (SQLToken token : entity.getSql()) {
            final String variableName = variableByPosition.get(token.getPosition());
            if (variableName != null) {
                if (!toReplace.containsKey(variableName)) {
                    throw new IllegalArgumentException(String.format("Argument: %s not found!", variableName));
                }
                dropCloseParen = appendParameter(sql, parameters, toReplace.get(variableName));
            } else if (dropCloseParen) {
                sql.append(withoutFirstCloseParen(token.getData()));
                dropCloseParen = false;
            } else {
                sql.append(token.getData());
            }
        }

        final BoundSql boundSql = new BoundSql(sql.toString(), parameters);
        traceQuery(boundSql);
        return boundSql;
    }

    private String withoutFirstCloseParen(String data) {
        int i = 0;
        while (i < data.length() && Character.isWhitespace(data.charAt(i))) {
            i++;
        }
        if (i < data.length() && data.charAt(i) == ')') {
            return data.substring(0, i) + data.substring(i + 1);
        }
        return data;
    }

    private void traceQuery(BoundSql boundSql) {
        if (variables.isSqlTracingEnabled()) {
            logger.info(QUERY_TRACING_PATTERN.formatted(boundSql.getSql(), boundSql.getParameters()));
        }
    }
}
