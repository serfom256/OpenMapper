package io.github.serfom256.unit.core.query;

import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.core.query.source.mapping.impl.InputMapperImpl;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InputMapperImplTest {

    @Mock SQLBuilder sqlBuilder;
    @Mock OpenMapperGlobalEnvironmentVariables variables;
    @Mock SQLRecord sqlRecord;

    private final BoundSql boundSql = new BoundSql("SELECT ?", List.of(1));

    @Test
    void mapSql_delegatesToSqlBuilder() {
        when(sqlBuilder.buildSql(any(), any())).thenReturn(boundSql);
        InputMapperImpl mapper = new InputMapperImpl(sqlBuilder, variables);

        BoundSql result = mapper.mapSql(sqlRecord, Map.of("id", 1));
        assertSame(boundSql, result);
        verify(sqlBuilder).buildSql(sqlRecord, Map.of("id", 1));
    }

    @Test
    void mapSql_withQueryTracingEnabled_logsQuery() {
        when(sqlBuilder.buildSql(any(), any())).thenReturn(boundSql);
        when(variables.isSqlQueryTracingEnabled()).thenReturn(true);
        InputMapperImpl mapper = new InputMapperImpl(sqlBuilder, variables);

        assertNotNull(mapper.mapSql(sqlRecord, Map.of()));
    }

    @Test
    void mapSql_withQueryTracingDisabled_noLog() {
        when(sqlBuilder.buildSql(any(), any())).thenReturn(boundSql);
        when(variables.isSqlQueryTracingEnabled()).thenReturn(false);
        InputMapperImpl mapper = new InputMapperImpl(sqlBuilder, variables);

        assertNotNull(mapper.mapSql(sqlRecord, Map.of()));
    }
}
