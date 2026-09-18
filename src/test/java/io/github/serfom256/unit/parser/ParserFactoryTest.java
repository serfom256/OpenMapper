package io.github.serfom256.unit.parser;

import io.github.serfom256.parser.Parser;
import io.github.serfom256.parser.factory.DefaultParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParserFactoryTest {

    @Test
    void getParser_returnsNonNull() {
        DefaultParserFactory factory = new DefaultParserFactory();
        Parser parser = factory.getParser("test.fsql", "findAll = { SELECT 1 }");
        assertNotNull(parser);
    }

    @Test
    void getParser_parsesSuccessfully() {
        DefaultParserFactory factory = new DefaultParserFactory();
        Parser parser = factory.getParser("test.fsql", "q = { SELECT 1 }");
        assertFalse(parser.parse().isEmpty());
    }

    @Test
    void getParser_emptyContent() {
        DefaultParserFactory factory = new DefaultParserFactory();
        Parser parser = factory.getParser("test.fsql", "");
        assertTrue(parser.parse().isEmpty());
    }
}
