package io.github.serfom256.parser.factory;

import io.github.serfom256.parser.Parser;

public interface ParserFactory {

    Parser getParser(final String fileName, final String fileContent);
}
