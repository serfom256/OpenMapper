package com.openmapper.parser;

import com.openmapper.exceptions.fsql.FsqlParsingException;
import com.openmapper.parser.model.SQLProcedure;

import java.util.List;

public interface Parser {

    List<SQLProcedure> parse() throws FsqlParsingException;

}
