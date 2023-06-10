package com.openmapper.common.parser;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.exceptions.fsql.FsqlParsingException;

import java.io.File;
import java.util.List;

public interface Parser {

    List<SQLProcedure> parse(File file) throws FsqlParsingException;

}
