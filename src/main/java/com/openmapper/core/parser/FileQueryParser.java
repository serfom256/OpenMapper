package com.openmapper.core.parser;

import com.openmapper.exceptions.FsqlParsingException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

@Component
public class FileQueryParser {

    public Map<String, String> parseFile(final List<String> fileContent, final String fileName) throws FsqlParsingException {
        Map<String, String> parsed = new HashMap<>();
        ListIterator<String> iterator = fileContent.listIterator();
        while (iterator.hasNext()) {
            tryAdvance(iterator);
            if (!iterator.hasNext()) break;
            String functionName = extractFunctionName(iterator);
            String functionBody = extractFunctionBlock(iterator);
            if (functionName.length() == 0 || functionBody.length() == 0) {
                throw new FsqlParsingException(fileName, iterator.previousIndex());
            }
            parsed.put(functionName, functionBody);
        }
        return parsed;
    }

    private void tryAdvance(ListIterator<String> iterator) {
        while (iterator.hasNext()) {
            String line = iterator.next();
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
}
