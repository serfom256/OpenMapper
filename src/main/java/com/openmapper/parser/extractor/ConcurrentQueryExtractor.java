package com.openmapper.parser.extractor;

import com.openmapper.common.util.FileUtil;
import com.openmapper.exceptions.fsql.FsqlParsingException;
import com.openmapper.exceptions.fsql.InvalidFileFormatException;
import com.openmapper.parser.factory.DefaultParserFactory;
import com.openmapper.parser.model.SQLProcedure;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.openmapper.config.OpenMapperGlobalConstants.FILE_EXTENSION;

public class ConcurrentQueryExtractor implements Extractor {

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final DefaultParserFactory parserFactory;

    public ConcurrentQueryExtractor(DefaultParserFactory parserFactory) {
        this.parserFactory = parserFactory;
    }

    @Override
    public List<Future<List<SQLProcedure>>> extract(File file) throws FsqlParsingException {
        return extractTree(file);
    }

    private List<SQLProcedure> parseFile(File file) {
        if (!file.getName().endsWith(FILE_EXTENSION.value())) {
            throw new InvalidFileFormatException(file.getName());
        }
        return parserFactory.getParser(file.getName(), FileUtil.readFile(file)).parse();
    }

    private List<Future<List<SQLProcedure>>> extractTree(File file) {
        if (canParse(file)) {
            return Collections.singletonList(
                    CompletableFuture.supplyAsync(() -> parseFile(file), executorService)
            );
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
