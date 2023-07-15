
package com.openmapper.common.parser;

import com.openmapper.common.entity.CharLocation;
import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.common.entity.SpanLocation;
import com.openmapper.exceptions.fsql.FsqlParsingException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class FileParser implements Parser {

    private static final String COMMENT = "#";
    private static final String EMPTY_BODY = "";

    private final String fileName;
    private final StringBuilder contents;
    private char currentChar;
    private int line = 0;
    private int column = 0;
    private int offset = 0;

    public FileParser(String fileName, StringBuilder contents) {
        if (contents.length() == 0) {
            this.currentChar = '\0';
        }

        this.contents = contents;
        this.fileName = fileName;
    }

    @Override
    public List<SQLProcedure> parse(final File file) throws FsqlParsingException {
        List<SQLProcedure> procedures = new ArrayList<>();

        while (!eof()) {
            procedures.add(parseProcedure());
        }

        return procedures;
    }

    public SQLProcedure parseProcedure() {
        String procedureName = parseProcedureName();

        expect('=');

        String query = parseQueryBlock();

        return new SQLProcedure(procedureName, query);
    }

    private String parseProcedureName() {
        skipWhitespaces();

        StringBuilder builder = new StringBuilder();

        while (validProcedureNameCharacter())
        {
            builder.append(currentChar);
            advance();
        }

        return builder.toString();
    }

    private String parseQueryBlock() {
        skipWhitespaces();

        expect('{');

        StringBuilder builder = new StringBuilder();

        while (currentChar != '}') {
            if (eof()) {
                throw new FsqlParsingException(fileName, currentCharSpanLocation());
            }

            if (singleLineCommentStart()) {
                consumeSingleLineComment();
            } else if (multiLineCommentStart()) {
                consumeMultiLineComment();
            } else {
                builder.append(currentChar);
            }

            advance();
        }

        advance();

        return builder.toString();
    }

    private void consumeSingleLineComment() {
        advanceTwice();

        while (currentChar != '\n' && !eof()) {
            advance();
        }
    }

    private void consumeMultiLineComment() {
        advanceTwice();

        while (!multiLineCommentEnd()) {
            if (eof()) {
                throw new FsqlParsingException(fileName, currentCharSpanLocation());
            }

            advance();
        }
    }

    private boolean singleLineCommentStart() {
        return currentChar == '-' && nextChar() == '-';
    }

    private boolean multiLineCommentStart() {
        return currentChar == '/' && nextChar() == '*';
    }

    private boolean multiLineCommentEnd() {
        return currentChar == '*' && nextChar() == '/';
    }

    private void expect(char c) {
        skipWhitespaces();

        if (currentChar != c) {
            throw new FsqlParsingException(fileName, currentCharSpanLocation());
        }

        advance();
    }

    private boolean validProcedureNameCharacter() {
        return ('A' <= currentChar && currentChar <= 'Z')
                || ('a' <= currentChar && currentChar <= 'z')
                || currentChar == '_' || currentChar == '-';
    }

    private void skipWhitespaces() {
        while (currentChar == '\t' || currentChar == ' ' || currentChar == '\r')
        {
            advance();
        }
    }

    private void advance() {
        if (currentChar == '\n') {
            line++;
            column = 0;
        } else {
            column++;
        }

        if (currentChar != '\0') {
            offset++;
        }

        if (eof()) {
            currentChar = contents.charAt(offset);
        } else {
            currentChar = '\0';
        }
    }

    private void advanceTwice() {
        advance();
        advance();
    }

    private boolean eof() {
        return offset == contents.length() - 1;
    }

    private CharLocation currentCharLocation() {
        return new CharLocation(line, column, offset);
    }

    private CharLocation nextCharLocation() {
        return new CharLocation(line, column, offset);
    }

    private SpanLocation currentCharSpanLocation() {
        return new SpanLocation(currentCharLocation(), nextCharLocation());
    }

    private char nextChar() {
        if (offset < contents.length()) {
            return contents.charAt(offset + 1);
        } else {
            return '\0';
        }
    }

    private List<String> readFile(final File file) {
        List<String> result = new ArrayList<>();
        try (FileInputStream fs = new FileInputStream(file); Scanner sc = new Scanner(fs)) {
            while (sc.hasNextLine()) {
                result.add(sc.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return result;
    }
}
