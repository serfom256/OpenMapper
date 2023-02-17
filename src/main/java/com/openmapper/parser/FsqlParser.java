package com.openmapper.parser;

import com.openmapper.exceptions.FsqlParsingException;
import com.openmapper.exceptions.InvalidFileFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FILE_EXTENSION;

@Component
public class FsqlParser implements AbstractParser {

    private final FileQueryParser parser;

    @Autowired
    public FsqlParser(FileQueryParser parser) {
        this.parser = parser;
    }

    @Override
    public Map<String, String> parseTree(File file) throws FsqlParsingException {
        Map<String, String> map = new HashMap<>();
        parseTree(map, file);
        return map;
    }

    private void parse(Map<String, String> map, File file) {
        if (!file.getName().endsWith(FILE_EXTENSION.getValue())) {
            throw new InvalidFileFormatException(file.getName());
        }
        try {
            map.putAll(parser.parseFile(readFile(file), file.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> readFile(File file) throws IOException {
        List<String> result = new ArrayList<>();
        try (FileInputStream fs = new FileInputStream(file); Scanner sc = new Scanner(fs)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.startsWith("#")) {
                    result.add("");
                } else {
                    result.add(line);
                }
            }
        }
        return result;
    }

    private void parseTree(Map<String, String> map, File file) {
        if (canParse(file)) {
            parse(map, file);
        } else if (file.isDirectory()) {
            parseTree(map, file);
        } else {
            throw new InvalidFileFormatException(file.getName());
        }
    }

    private boolean canParse(File file) {
        return file.isFile() && file.getName().endsWith(FILE_EXTENSION.getValue());
    }
}
