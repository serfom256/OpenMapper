package com.openmapper.common;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.common.parser.FileParser;
import com.openmapper.exceptions.fsql.FsqlParsingException;
import com.openmapper.exceptions.fsql.InvalidFileFormatException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FILE_EXTENSION;

public class QueryExtractor {

    private final FileParser parser = new FileParser();

    public List<SQLProcedure> extractContent(File file) throws FsqlParsingException {
        return extractTree(file);
    }

    private List<SQLProcedure> parseFile(File file) {
        if (!file.getName().endsWith(FILE_EXTENSION.value())) {
            throw new InvalidFileFormatException(file.getName());
        }
        return parser.parse(file);
    }


    private List<SQLProcedure> extractTree(File file) {
        if (canParse(file)) {
            return parseFile(file);
        } else if (file.isDirectory()) {
            return Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .flatMap(f -> extractTree(f).stream())
                    .collect(Collectors.toList());
        }
        throw new InvalidFileFormatException(file.getName());
    }

    private boolean canParse(File file) {
        return file.isFile() && file.getName().endsWith(FILE_EXTENSION.value());
    }
}
