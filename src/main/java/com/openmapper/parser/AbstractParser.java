package com.openmapper.parser;

import com.openmapper.exceptions.FsqlParsingException;

import java.io.File;
import java.util.Map;

public interface AbstractParser {

    Map<String, String> parseTree(File file) throws FsqlParsingException;

}
