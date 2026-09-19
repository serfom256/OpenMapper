package io.github.serfom256.unit.common;

import io.github.serfom256.common.SQLBuilder;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SQLBuilderTest {

    @Mock OpenMapperGlobalEnvironmentVariables variables;

    private SQLBuilder sqlBuilder;
    private final SourceMapper sourceMapper = new SourceMapper();

    @BeforeEach
    void setUp() {
        sqlBuilder = new SQLBuilder(variables);
    }

    private BoundSql build(String sql, Map<String, Object> params) {
        return sqlBuilder.buildSql(sourceMapper.map(sql), params);
    }

    private List<Integer> ids(int count) {
        return IntStream.rangeClosed(1, count).boxed().toList();
    }

    @Test
    void buildSql_singleVariable_emitsPlaceholderAndBindValue() {
        SQLRecord record = sourceMapper.map("SELECT * FROM users WHERE id = [id]");
        BoundSql result = sqlBuilder.buildSql(record, Map.of("id", 42));

        assertTrue(result.getSql().contains("?"));
        assertFalse(result.getSql().contains("42"));
        assertEquals(1, result.getParameters().size());
        assertEquals(42, result.getParameters().get(0));
    }

    @Test
    void buildSql_multipleVariables_bindInOrder() {
        SQLRecord record = sourceMapper.map("INSERT INTO users (name, age) VALUES ([name], [age])");
        BoundSql result = sqlBuilder.buildSql(record, Map.of("name", "John", "age", 25));

        assertEquals(2, result.getParameters().size());
        assertEquals("John", result.getParameters().get(0));
        assertEquals(25, result.getParameters().get(1));
        assertEquals(2, result.getSql().chars().filter(c -> c == '?').count());
    }

    @Test
    void buildSql_doesNotConcatenateValues_injectionPayloadStaysData() {
        SQLRecord record = sourceMapper.map("SELECT * FROM users WHERE name = [name]");
        BoundSql result = sqlBuilder.buildSql(record, Map.of("name", "x' OR '1'='1"));

        assertFalse(result.getSql().contains("OR"));
        assertFalse(result.getSql().contains("'"));
        assertEquals("x' OR '1'='1", result.getParameters().get(0));
    }

    @Test
    void buildSql_missingArgument_throws() {
        SQLRecord record = sourceMapper.map("SELECT * FROM users WHERE id = [id]");
        assertThrows(IllegalArgumentException.class,
                () -> sqlBuilder.buildSql(record, Map.of("wrong", 1)));
    }

    @Test
    void buildSql_tooFewArguments_throws() {
        SQLRecord record = sourceMapper.map("SELECT * FROM u WHERE a = [a] AND b = [b]");
        assertThrows(IllegalArgumentException.class,
                () -> sqlBuilder.buildSql(record, Map.of("a", 1)));
    }

    @Test
    void buildSql_extraArguments_allowed() {
        SQLRecord record = sourceMapper.map("SELECT * FROM users WHERE id = [id]");
        BoundSql result = sqlBuilder.buildSql(record, Map.of("id", 1, "extra", "value"));
        assertEquals(1, result.getParameters().size());
    }

    @Test
    void buildSql_noVariables_noParameters() {
        SQLRecord record = sourceMapper.map("SELECT * FROM users");
        BoundSql result = sqlBuilder.buildSql(record, Map.of());
        assertTrue(result.getSql().contains("SELECT"));
        assertTrue(result.getParameters().isEmpty());
    }

    @Test
    void buildSql_nullValue_boundAsNull() {
        SQLRecord record = sourceMapper.map("UPDATE users SET nickname = [nick] WHERE id = [id]");
        Map<String, Object> params = new HashMap<>();
        params.put("nick", null);
        params.put("id", 5);
        BoundSql result = sqlBuilder.buildSql(record, params);

        assertEquals(2, result.getParameters().size());
        assertNull(result.getParameters().get(0));
        assertEquals(5, result.getParameters().get(1));
    }

    @Test
    void buildSql_withTracingEnabled_doesNotThrow() {
        org.mockito.Mockito.when(variables.isSqlTracingEnabled()).thenReturn(true);
        SQLRecord record = sourceMapper.map("SELECT * FROM users WHERE id = [id]");
        assertDoesNotThrow(() -> sqlBuilder.buildSql(record, Map.of("id", 1)));
    }

    @Test
    @DisplayName("an empty IN list matches nothing instead of binding NULL")
    void emptyInListMatchesNothing() {
        BoundSql result = build("SELECT * FROM t WHERE id IN ([ids]) ORDER BY id", Map.of("ids", List.of()));

        assertEquals("SELECT * FROM t WHERE 1 = 0 ORDER BY id", result.getSql().replaceAll("\\s+", " ").trim());
        assertTrue(result.getParameters().isEmpty());
    }

    @Test
    @DisplayName("an empty NOT IN list excludes nothing rather than dropping every row")
    void emptyNotInListExcludesNothing() {
        BoundSql result = build("SELECT * FROM t WHERE id NOT IN ([ids]) ORDER BY id", Map.of("ids", List.of()));

        assertEquals("SELECT * FROM t WHERE 1 = 1 ORDER BY id", result.getSql().replaceAll("\\s+", " ").trim());
        assertTrue(result.getParameters().isEmpty());
    }

    @Test
    @DisplayName("an empty collection outside an IN clause still binds NULL")
    void emptyCollectionOutsideInClauseBindsNull() {
        BoundSql result = build("SELECT * FROM t WHERE name = [name]", Map.of("name", List.of()));

        assertTrue(result.getSql().contains("NULL"), result.getSql());
    }

    @Test
    @DisplayName("a list within the chunk size is emitted as one IN clause")
    void listWithinChunkSizeIsNotSplit() {
        when(variables.getInClauseChunkSize()).thenReturn(3);
        BoundSql result = build("SELECT * FROM t WHERE id IN ([ids])", Map.of("ids", ids(3)));

        assertEquals("SELECT * FROM t WHERE id IN (?, ?, ?)", result.getSql().replaceAll("\\s+", " ").trim());
        assertEquals(3, result.getParameters().size());
    }

    @Test
    @DisplayName("a list over the chunk size is split into OR-ed IN clauses")
    void longListIsChunked() {
        when(variables.getInClauseChunkSize()).thenReturn(2);
        BoundSql result = build("SELECT * FROM t WHERE t.id IN ([ids])", Map.of("ids", ids(5)));

        assertEquals("SELECT * FROM t WHERE t.id IN (?, ?) OR t.id IN (?, ?) OR t.id IN (?)",
                result.getSql().replaceAll("\\s+", " ").trim());
        assertEquals(List.of(1, 2, 3, 4, 5), result.getParameters());
    }

    @Test
    @DisplayName("a long NOT IN list is split with AND, not OR")
    void longNotInListIsChunkedWithAnd() {
        when(variables.getInClauseChunkSize()).thenReturn(2);
        BoundSql result = build("SELECT * FROM t WHERE t.id NOT IN ([ids])", Map.of("ids", ids(4)));

        assertEquals("SELECT * FROM t WHERE t.id NOT IN (?, ?) AND t.id NOT IN (?, ?)",
                result.getSql().replaceAll("\\s+", " ").trim());
    }

    @Test
    @DisplayName("chunking keeps a function operand intact")
    void chunkingKeepsFunctionOperand() {
        when(variables.getInClauseChunkSize()).thenReturn(2);
        BoundSql result = build("SELECT * FROM t WHERE LOWER(t.name) IN ([names])",
                Map.of("names", List.of("a", "b", "c")));

        assertEquals("SELECT * FROM t WHERE LOWER(t.name) IN (?, ?) OR LOWER(t.name) IN (?)",
                result.getSql().replaceAll("\\s+", " ").trim());
    }

    @Test
    @DisplayName("chunking is off when the chunk size is not positive")
    void chunkingDisabled() {
        when(variables.getInClauseChunkSize()).thenReturn(0);
        BoundSql result = build("SELECT * FROM t WHERE id IN ([ids])", Map.of("ids", ids(4)));

        assertEquals("SELECT * FROM t WHERE id IN (?, ?, ?, ?)", result.getSql().replaceAll("\\s+", " ").trim());
    }
}
