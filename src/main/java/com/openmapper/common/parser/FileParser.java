package com.openmapper.common.parser;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.exceptions.fsql.FsqlParsingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class FileParser implements Parser {

    private static final String COMMENT = "#";
    private static final String EMPTY_BODY = "";

    @Override
    public List<SQLProcedure> parse(final File file) throws FsqlParsingException {
        return null;
    }

    private List<String> readFile(final File file) {
        List<String> result = new ArrayList<>();
        try (FileInputStream fs = new FileInputStream(file); Scanner sc = new Scanner(fs)) {
            while (sc.hasNextLine()) {
                result.add(sc.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return result;
    }
}
