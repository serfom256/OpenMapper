package com.openmapper.parser.factory;

import org.springframework.stereotype.Component;

import com.openmapper.parser.DefaultParserImpl;
import com.openmapper.parser.Parser;

@Component
public class DefaultParserFactory implements ParserFactory {

    @Override
    public Parser getParser(final String fileName, final String fileContent) {
        return new DefaultParserImpl(fileName, fileContent);
    }
}
