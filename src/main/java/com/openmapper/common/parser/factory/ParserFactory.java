package com.openmapper.common.parser.factory;

import com.openmapper.common.parser.Parser;

public interface ParserFactory {

    Parser getParser(final String fileName, final String fileContent);
}
