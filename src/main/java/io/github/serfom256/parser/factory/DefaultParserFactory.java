package io.github.serfom256.parser.factory;

import io.github.serfom256.parser.DefaultParserImpl;
import io.github.serfom256.parser.Parser;

public class DefaultParserFactory implements ParserFactory {

    @Override
    public Parser getParser(final String fileName, final String fileContent) {
        return new DefaultParserImpl(fileName, fileContent);
    }
}
