
package com.openmapper.parser.impl;

import com.openmapper.exceptions.fsql.FsqlParsingException;
import com.openmapper.parser.Parser;
import com.openmapper.parser.model.CharLocation;
import com.openmapper.parser.model.SQLProcedure;
import com.openmapper.parser.model.SpanLocation;

import java.util.ArrayList;
import java.util.List;

public class DefaultParserImpl implements Parser {

    private static final char EOF = '\0';
    private static final char NEW_LINE = '\n';
    private static final char CARRIAGE_RETURN = '\r';
    private static final char EMPTY_SPACE = ' ';
    private static final char TAB = '\t';
    private static final char SQL_COMMENT = '#';

    private final String fileName;
    private final StringBuilder contents;
    private char currentChar;
    private int line = 1;
    private int column = 0;
    private int offset = 0;

    public DefaultParserImpl(String fileName, String contents) {
        this.currentChar = contents.length() == 0 ? EOF : contents.charAt(0);
        this.contents = new StringBuilder(contents);
        this.fileName = fileName;
    }

    @Override
    public List<SQLProcedure> parse() throws FsqlParsingException {
        final List<SQLProcedure> procedures = new ArrayList<>();
        skipWhitespaces();
        consumeFSqlComments();
        while (!isEof()) {
            procedures.add(parseProcedure());
            skipWhitespaces();
            consumeFSqlComments();
        }

        return procedures;
    }

    public SQLProcedure parseProcedure() {

        final String procedureName = parseProcedureName();

        consumeFSqlComments();

        expect('=');

        consumeFSqlComments();

        final String queryBlock = parseQueryBlock();

        return new SQLProcedure(procedureName, queryBlock);
    }

    private String parseProcedureName() {
        skipWhitespaces();

        final StringBuilder builder = new StringBuilder();

        while (isValidProcedureName()) {
            builder.append(currentChar);
            advance();
        }

        return builder.toString();
    }

    private String parseQueryBlock() {
        skipWhitespaces();

        expect('{');

        int newlineStart = 0;

        final StringBuilder queryBody = new StringBuilder();

        while (currentChar != '}') {
            if (isEof()) {
                throw new FsqlParsingException(fileName, getCharSpanLocation());
            }
            if (singleLineCommentStart() || sqlCommentStart()) {
                consumeSingleLineComment();
            } else if (multiLineCommentStart()) {
                consumeMultiLineComment();
            } else if (currentChar != NEW_LINE && currentChar != CARRIAGE_RETURN) {
                queryBody.append(currentChar);
            } else if (currentChar == NEW_LINE) {
                queryBody
                        .replace(newlineStart,
                                queryBody.length(),
                                queryBody.substring(newlineStart, queryBody.length()).strip()
                        )
                        .append(EMPTY_SPACE);
                newlineStart = queryBody.length();
            }
            advance();
        }

        advance();

        return queryBody.toString().strip();
    }

    private void consumeFSqlComments() {
        while (sqlCommentStart()) {
            consumeSingleLineComment();
        }
    }

    private void consumeSingleLineComment() {
        advanceTwice();

        while (currentChar != NEW_LINE && !isEof()) {
            advance();
        }
    }

    private void consumeMultiLineComment() {
        advanceTwice();

        while (!multiLineCommentEnd()) {
            if (isEof()) {
                throw new FsqlParsingException(fileName, getCharSpanLocation());
            }
            advance();
        }
    }

    private boolean sqlCommentStart() {
        return currentChar == SQL_COMMENT;
    }

    private boolean singleLineCommentStart() {
        return (currentChar == '-' && nextChar() == '-') || currentChar == SQL_COMMENT;
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
            throw new FsqlParsingException(fileName, getCharSpanLocation());
        }

        advance();
    }

    private boolean isValidProcedureName() {
        return Character.isAlphabetic(currentChar) || Character.isDigit(currentChar) || currentChar == '_' || currentChar == '-' || currentChar == '.';
    }

    private void skipWhitespaces() {
        while (currentChar == TAB || currentChar == EMPTY_SPACE || currentChar == NEW_LINE || currentChar == CARRIAGE_RETURN) {
            advance();
        }
    }

    private void advance() {
        if (currentChar == NEW_LINE) {
            line++;
            column = 0;
        } else {
            column++;
        }
        currentChar = isEof() ? EOF : contents.charAt(++offset);
    }

    private void advanceTwice() {
        advance();
        advance();
    }

    private boolean isEof() {
        return offset >= contents.length() - 1;
    }

    private CharLocation currentCharLocation() {
        return new CharLocation(line, column, offset);
    }

    private CharLocation nextCharLocation() {
        return new CharLocation(line, column, offset);
    }

    private SpanLocation getCharSpanLocation() {
        return new SpanLocation(currentCharLocation(), nextCharLocation());
    }

    private char nextChar() {
        return offset < contents.length() ? contents.charAt(offset + 1) : EOF;
    }
}
