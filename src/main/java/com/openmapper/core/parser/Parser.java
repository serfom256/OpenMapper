package com.openmapper.core.parser;

import com.openmapper.exceptions.FsqlParsingException;

import java.io.File;
import java.util.Map;

public interface Parser {

    Map<String, String> parseTree(File file) throws FsqlParsingException;

}
