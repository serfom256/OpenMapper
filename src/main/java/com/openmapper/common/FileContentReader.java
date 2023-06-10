package com.openmapper.common;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.config.OpenMapperGlobalContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileContentReader {

    private final QueryExtractor queryExtractor = new QueryExtractor();

    private final OpenMapperGlobalContext globalContext;
    private static final Logger logger = LoggerFactory.getLogger(FileContentReader.class);

    @Autowired
    public FileContentReader(OpenMapperGlobalContext globalContext) {
        this.globalContext = globalContext;
    }

    public List<SQLProcedure> readFiles(final List<String> files) {
        if (globalContext.isLogging()) files.forEach(f -> logger.debug("File loaded: {}", f));
        return files.stream()
                .flatMap(file -> queryExtractor.extractContent(new File(file)).stream())
                .collect(Collectors.toList());
    }
}
