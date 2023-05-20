package com.openmapper.core.processors.parser;

import com.openmapper.exceptions.fsql.FsqlParsingException;

import java.io.File;
import java.util.Map;

public interface Parser {

    Map<String, String> parseTree(File file) throws FsqlParsingException;

}
