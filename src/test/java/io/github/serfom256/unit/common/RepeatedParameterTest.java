package io.github.serfom256.unit.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLRecord;

class RepeatedParameterTest {

    private final SourceMapper sourceMapper = new SourceMapper();
    private final SQLBuilder sqlBuilder =
            new SQLBuilder(org.mockito.Mockito.mock(OpenMapperGlobalEnvironmentVariables.class));

    private BoundSql build(String sql, Map<String, Object> params) {
        SQLRecord record = sourceMapper.map(sql);
        return sqlBuilder.buildSql(record, params);
    }

    @Test
    @DisplayName("a parameter used twice is bound at both positions")
    void parameterUsedTwice() {
        BoundSql bound = build("SELECT * FROM t WHERE a = [x] OR b = [x]", Map.of("x", 7));

        assertThat(bound.getSql()).isEqualTo("SELECT * FROM t WHERE a = ? OR b = ?");
        assertThat(bound.getParameters()).containsExactly(7, 7);
    }

    @Test
    @DisplayName("an upsert repeating every column binds each occurrence")
    void upsertShape() {
        BoundSql bound = build(
                "INSERT INTO t (a, b) VALUES ([a], [b]) ON CONFLICT (a) DO UPDATE SET b = [b]",
                Map.of("a", 1, "b", 2));

        assertThat(bound.getSql())
                .isEqualTo("INSERT INTO t (a, b) VALUES (?, ?) ON CONFLICT (a) DO UPDATE SET b = ?");
        assertThat(bound.getParameters()).containsExactly(1, 2, 2);
    }

    @Test
    @DisplayName("distinct parameters keep their own values and order")
    void distinctParameters() {
        BoundSql bound = build("SELECT * FROM t WHERE a = [x] AND b = [y]", Map.of("x", 1, "y", 2));

        assertThat(bound.getParameters()).containsExactly(1, 2);
    }
}
