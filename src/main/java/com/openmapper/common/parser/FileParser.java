package com.openmapper.common.parser;

import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.exceptions.fsql.FsqlParsingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class FileParser implements Parser {

    @Override
    public List<SQLProcedure> parse(final File file) throws FsqlParsingException {
        final List<String> fileContent = readFile(file);
        final Map<String, SQLProcedure> parsed = new HashMap<>();
        final ListIterator<String> iterator = fileContent.listIterator();
        while (iterator.hasNext()) {
            tryAdvance(iterator);
            if (!iterator.hasNext()) break;
            final String functionName = extractFunctionName(iterator);
            final String procedureBody = extractFunctionBlock(iterator);
            if (functionName.length() == 0 || procedureBody.length() == 0) {
                throw new FsqlParsingException(file.getName(), iterator.previousIndex());
            }
            parsed.put(functionName, new SQLProcedure(functionName, procedureBody));
        }
        return List.copyOf(parsed.values());
    }

    private void tryAdvance(ListIterator<String> iterator) {
        while (iterator.hasNext()) {
            final String line = iterator.next();
            int pos = 0;
            while (pos < line.length() && line.charAt(pos) == ' ') {
                pos++;
            }
            if (pos != line.length()) {
                iterator.previous();
                return;
            }
        }
    }

    private String extractFunctionBlock(ListIterator<String> iterator) {
        if (!iterator.hasNext()) return "";
        String nxt = iterator.next().strip();
        while (iterator.hasNext() && nxt.length() == 0) {
            nxt = iterator.next().strip();
        }
        if (nxt.charAt(0) != '{') return "";
        iterator.previous();
        String block = iterator.next().replace("{", "").trim();
        StringBuilder function = new StringBuilder();
        int idx = block.indexOf("}");
        while (idx < 0 && iterator.hasNext()) {
            function.append(block.trim().replaceAll("\\s{2,}", " ")).append(' ');
            block = iterator.next();
            idx = block.indexOf("}");
        }
        if (idx > 0 && function.length() == 0) {
            function.append(block.replace("{", "").replace("}", "").replaceAll("\\s{2,}", " "));
        }
        iterator.remove();
        iterator.add(block.substring(idx + 1));
        iterator.previous();
        return function.toString().trim();
    }

    private String extractFunctionName(ListIterator<String> iterator) {
        String name = iterator.next();
        int idx = name.indexOf("=");
        while (idx < 0 && iterator.hasNext()) {
            name = iterator.next();
            idx = name.indexOf("=");
        }
        if (idx < 0) {
            return "";
        }
        iterator.remove();
        iterator.add(name.substring(idx + 1));
        iterator.previous();
        return name.substring(0, idx).strip();
    }

    private List<String> readFile(final File file) {
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
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return result;
    }
}
