package io.github.serfom256.parser;

import io.github.serfom256.exceptions.fsql.FsqlParsingException;
import io.github.serfom256.parser.model.SQLProcedure;

import java.util.List;

public interface Parser {

    List<SQLProcedure> parse() throws FsqlParsingException;

}
