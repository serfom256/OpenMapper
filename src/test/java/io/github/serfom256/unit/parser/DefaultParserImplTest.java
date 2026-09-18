package io.github.serfom256.unit.parser;

import io.github.serfom256.exceptions.fsql.FsqlParsingException;
import io.github.serfom256.parser.DefaultParserImpl;
import io.github.serfom256.parser.model.SQLProcedure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultParserImplTest {

    @Test
    void parseSingleProcedure() {
        String input = "findById = { SELECT * FROM users WHERE id = 1 }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
        assertEquals("findById", result.get(0).getFunctionName());
        assertTrue(result.get(0).getFunctionBody().contains("SELECT"));
    }

    @Test
    void parseMultipleProcedures() {
        String input = """
                findAll = { SELECT * FROM users }
                findById = { SELECT * FROM users WHERE id = 1 }
                """;
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(2, result.size());
        assertEquals("findAll", result.get(0).getFunctionName());
        assertEquals("findById", result.get(1).getFunctionName());
    }

    @Test
    void parseProcedureWithVariables() {
        String input = "findById = { SELECT * FROM users WHERE id = [id] AND name = [name] }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
        assertTrue(result.get(0).getFunctionBody().contains("[id]"));
        assertTrue(result.get(0).getFunctionBody().contains("[name]"));
    }

    @Test
    void parseEmptyInput() {
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", "");
        List<SQLProcedure> result = parser.parse();
        assertTrue(result.isEmpty());
    }

    @Test
    void parseWithSingleLineComments() {
        String input = """
                # This is a comment
                findAll = { SELECT * FROM users }
                """;
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
    }

    @Test
    void parseWithDashComments() {
        String input = "findAll = {\n-- this is a SQL comment\nSELECT * FROM users\n}";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
        assertFalse(result.get(0).getFunctionBody().contains("--"));
    }

    @Test
    void parseWithMultiLineComments() {
        String input = "findAll = {\n/* multi\nline\ncomment */\nSELECT * FROM users\n}";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
        assertFalse(result.get(0).getFunctionBody().contains("/*"));
    }

    @Test
    void parseProcedureWithMultipleLines() {
        String input = "findAll = {\n  SELECT *\n  FROM users\n  WHERE active = true\n}";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
        String body = result.get(0).getFunctionBody();
        assertTrue(body.contains("SELECT"));
        assertTrue(body.contains("FROM users"));
        assertTrue(body.contains("WHERE active = true"));
    }

    @Test
    void parseWithWhitespaceAroundEquals() {
        String input = "findAll   =   { SELECT * FROM users }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals(1, result.size());
        assertEquals("findAll", result.get(0).getFunctionName());
    }

    @Test
    void parseUnclosedBlock_throws() {
        String input = "findAll = { SELECT * FROM users";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        assertThrows(FsqlParsingException.class, parser::parse);
    }

    @Test
    void parseMissingEquals_throws() {
        String input = "findAll { SELECT * FROM users }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        assertThrows(FsqlParsingException.class, parser::parse);
    }

    @Test
    void parseProcedureNameWithUnderscores() {
        String input = "find_by_id = { SELECT 1 }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals("find_by_id", result.get(0).getFunctionName());
    }

    @Test
    void parseProcedureNameWithDots() {
        String input = "user.findById = { SELECT 1 }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals("user.findById", result.get(0).getFunctionName());
    }

    @Test
    void parseUnclosedMultiLineComment_throws() {
        String input = "findAll = { /* unclosed comment SELECT 1 }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        assertThrows(FsqlParsingException.class, parser::parse);
    }

    @Test
    void parseStripsLeadingTrailingWhitespaceFromBody() {
        String input = "q = {   SELECT 1   }";
        DefaultParserImpl parser = new DefaultParserImpl("test.fsql", input);
        List<SQLProcedure> result = parser.parse();
        assertEquals("SELECT 1", result.get(0).getFunctionBody());
    }
}
