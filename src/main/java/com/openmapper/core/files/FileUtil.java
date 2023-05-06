package com.openmapper.core.files;

import com.openmapper.config.OpenMapperGlobalContext;
import com.openmapper.core.files.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileUtil {

    private final Parser parser;

    private final OpenMapperGlobalContext globalContext;
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    @Autowired
    public FileUtil(Parser parser, OpenMapperGlobalContext globalContext) {
        this.parser = parser;
        this.globalContext = globalContext;
    }

    public Map<String, String> findFilesAndParse(List<String> files) {
        if (globalContext.isLogging()) files.forEach(f -> logger.debug("File loaded: {}", f));
        List<Map<String, String>> parsed = files.stream()
                .map(fsqlFile -> parser.parseTree(new File(fsqlFile)))
                .collect(Collectors.toList());

        Map<String, String> map = new HashMap<>();
        for (Map<String, String> m : parsed) {
            map.putAll(m);
        }
        return map;
    }
}
