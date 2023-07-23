package com.openmapper.common;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.common.extractor.ConcurrentQueryExtractor;
import com.openmapper.common.extractor.Extractor;
import com.openmapper.common.parser.factory.DefaultParserFactory;
import com.openmapper.config.OpenMapperGlobalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class FileContentReader {

    private final Extractor queryExtractor;

    private final OpenMapperGlobalContext globalContext;
    private static final Logger logger = LoggerFactory.getLogger(FileContentReader.class);

    @Autowired
    public FileContentReader(OpenMapperGlobalContext globalContext, DefaultParserFactory parserFactory) {
        this.globalContext = globalContext;
        this.queryExtractor = new ConcurrentQueryExtractor(parserFactory);
    }

    public List<SQLProcedure> readFiles(final List<String> files) {
        if (globalContext.isLogging()) files.forEach(f -> logger.debug("File loaded: {}", f));

        final List<SQLProcedure> procedures = new ArrayList<>();

        List<Future<List<SQLProcedure>>> parsedResult = files.stream()
                .flatMap(file -> queryExtractor.extract(new File(file)).stream())
                .collect(Collectors.toList());

        for (var future : parsedResult) {
            try {
                procedures.addAll(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return procedures;
    }
}
