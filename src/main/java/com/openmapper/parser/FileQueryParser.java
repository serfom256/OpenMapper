package com.openmapper.parser;

import com.openmapper.exceptions.FsqlParsingException;
import com.openmapper.parser.entity.SqlTemporaryEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Component
public class FileQueryParser {

    private final List<Character> brackets;

    public FileQueryParser() {
        brackets = new ArrayList<>(Arrays.asList('{', '}'));
    }


    public Map<String, String> parseFile(File file) throws FsqlParsingException, IOException {
        Map<String, String> parsed = new HashMap<>();
        List<String> fileContent = readFile(file);
        fileContent.removeIf(s -> s.length() == 0);
        ListIterator<String> iterator = fileContent.listIterator();
        while (iterator.hasNext()) {
            int pos = moveCursor(iterator);
            SqlTemporaryEntity scope = parseScope(file.getAbsolutePath(), iterator, pos);
            parsed.put(scope.getName(), scope.getSql());
        }
        return parsed;
    }

    private List<String> readFile(File file) throws IOException {
        List<String> result = new ArrayList<>();
        try (FileInputStream fs = new FileInputStream(file); Scanner sc = new Scanner(fs)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (!line.startsWith("#")) result.add(line);
            }
        }
        return result;
    }

    private Integer moveCursor(ListIterator<String> iterator) {
        int linePosition = 0;
        while (iterator.hasNext()) {
            String line = iterator.next();
            int pos = 0;
            while (pos < line.length() && line.charAt(pos) == ' ') {
                pos++;
            }
            if (pos != line.length()) {
                iterator.previous();
                linePosition = pos;
                break;
            }
        }
        return linePosition;
    }

    // fixme
    private SqlTemporaryEntity parseScope(String fileName, ListIterator<String> iterator, int position) {
        SqlTemporaryEntity entity = new SqlTemporaryEntity();
        final Deque<Character> stack = new ArrayDeque<>(5);
        stack.push(' ');
        StringBuilder sql = new StringBuilder();
        while (iterator.hasNext() && stack.size() < 3) {
            String line = iterator.next();
            for (int i = 0; i < line.length() && stack.size() < 3; i++) {
                Character c = line.charAt(i);
                if (c == '=' && entity.getName() == null) {
                    entity.setName(line.substring(position, i));
                } else if (brackets.contains(c)) {
                    Character previous = stack.peekFirst();
                    if (Objects.equals(previous, c)) {
                        throw new FsqlParsingException(fileName, iterator.previousIndex(), position);
                    }
                    stack.push(c);
                } else if (stack.size() > 1) {
                    sql.append(c);
                }
            }
        }
        if (stack.size() != 3 || !stack.containsAll(brackets)) {
            throw new FsqlParsingException(fileName, iterator.previousIndex());
        }
        entity.setSql(sql.toString());
        return entity;
    }
}
