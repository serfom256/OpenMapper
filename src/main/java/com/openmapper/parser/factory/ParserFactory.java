package com.openmapper.parser.factory;

import com.openmapper.parser.Parser;

public interface ParserFactory {

    Parser getParser(final String fileName, final String fileContent);
}
