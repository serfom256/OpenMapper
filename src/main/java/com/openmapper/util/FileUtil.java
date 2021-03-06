package com.openmapper.util;

import com.openmapper.parser.AbstractParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileUtil {

    private final AbstractParser parser;

    @Autowired
    public FileUtil(AbstractParser parser) {
        this.parser = parser;
    }

    public Map<String, String> findFilesAndParse(List<String> files) {
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
