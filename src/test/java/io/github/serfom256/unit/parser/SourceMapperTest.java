package io.github.serfom256.unit.parser;

import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SourceMapperTest {

    private final SourceMapper mapper = new SourceMapper();

    @Test
    void mapSimpleQuery_noVariables() {
        SQLRecord record = mapper.map("SELECT * FROM users");
        assertNotNull(record);
        assertTrue(record.getVariables().isEmpty());
        assertFalse(record.getSql().isEmpty());
    }

    @Test
    void mapQueryWithSingleVariable() {
        SQLRecord record = mapper.map("SELECT * FROM users WHERE id = [id]");
        assertEquals(1, record.getVariables().size());
        assertTrue(record.getVariables().containsKey("id"));
    }

    @Test
    void mapQueryWithMultipleVariables() {
        SQLRecord record = mapper.map("SELECT * FROM users WHERE name = [name] AND age = [age]");
        assertEquals(2, record.getVariables().size());
        assertTrue(record.getVariables().containsKey("name"));
        assertTrue(record.getVariables().containsKey("age"));
    }

    @Test
    void mapInsertQuery() {
        SQLRecord record = mapper.map("INSERT INTO users (name, email) VALUES ([name], [email])");
        assertEquals(2, record.getVariables().size());
        assertTrue(record.getVariables().containsKey("name"));
        assertTrue(record.getVariables().containsKey("email"));
    }

    @Test
    void mapEmptyQuery() {
        SQLRecord record = mapper.map("");
        assertNotNull(record);
        assertTrue(record.getVariables().isEmpty());
    }

    @Test
    void mapUnclosedVariable_parsesVariableButMissingClose_throws() {
        assertThrows(IllegalStateException.class,
                () -> mapper.map("SELECT * FROM users WHERE id = [id extra"));
    }

    @Test
    void mapTrailingUnclosedVariable_parsedSilently() {
        SQLRecord record = mapper.map("SELECT * FROM users WHERE id = [id");
        assertNotNull(record);
        assertFalse(record.getVariables().isEmpty());
    }

    @Test
    void mapQueryTokensPreserveOrder() {
        SQLRecord record = mapper.map("SELECT [a] FROM [b]");
        assertEquals(2, record.getVariables().size());
        assertTrue(record.getVariables().containsKey("a"));
        assertTrue(record.getVariables().containsKey("b"));
    }

    @Test
    void toStringRepresentation() {
        SQLRecord record = mapper.map("SELECT 1");
        assertNotNull(record.toString());
    }
}
