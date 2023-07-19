package com.openmapper.common.parser;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.exceptions.fsql.FsqlParsingException;

import java.util.List;

public interface Parser {

    List<SQLProcedure> parse() throws FsqlParsingException;

}
