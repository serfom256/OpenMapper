package com.openmapper.common.parser.factory;

import com.openmapper.common.parser.impl.DefaultParserImpl;
import com.openmapper.common.parser.Parser;
import org.springframework.stereotype.Component;

@Component
public class DefaultParserFactory implements ParserFactory {

    @Override
    public Parser getParser(final String fileName, final String fileContent) {
        return new DefaultParserImpl(fileName, fileContent);
    }
}
