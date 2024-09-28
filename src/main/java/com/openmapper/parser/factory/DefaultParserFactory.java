package com.openmapper.parser.factory;

import org.springframework.stereotype.Component;

import com.openmapper.parser.Parser;
import com.openmapper.parser.impl.DefaultParserImpl;

@Component
public class DefaultParserFactory implements ParserFactory {

    @Override
    public Parser getParser(final String fileName, final String fileContent) {
        return new DefaultParserImpl(fileName, fileContent);
    }
}
