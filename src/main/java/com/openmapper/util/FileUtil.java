package com.openmapper.util;

import com.openmapper.core.parser.Parser;
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

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    @Autowired
    public FileUtil(Parser parser) {
        this.parser = parser;
    }

    public Map<String, String> findFilesAndParse(List<String> files) {
        files.forEach(f -> logger.debug("File loaded: " + f));
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
