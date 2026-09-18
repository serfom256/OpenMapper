package io.github.serfom256.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.serfom256.parser.model.SQLRecord;
import io.github.serfom256.parser.model.SQLToken;

public class SourceMapper {

    private static final String REGEX = "((?=\\W)|(?<=\\W))";

    private static final String VARIABLE_START_IDENTIFIER = "[";
    private static final String VARIABLE_CLOSE_IDENTIFIER = "]";

    public SQLRecord map(final String sqlProcedure) {

        int position = 0;

        boolean expectedVariable = false;
        boolean expectedCloseIdentifier = false;

        final String[] tokens = sqlProcedure.split(REGEX);
        final List<SQLToken> tokenList = new ArrayList<>();

        final Map<String, SQLToken> variables = new HashMap<>();
        final Map<Integer, String> variablePositions = new HashMap<>();
        final StringBuilder sequence = new StringBuilder();

        while (position < tokens.length) {

            final String token = tokens[position];

            if (token.equals(VARIABLE_START_IDENTIFIER)) { // check variable opens
                expect(VARIABLE_START_IDENTIFIER, token, "Expected open bracket ']' before the variable");
                expectedVariable = true;

            } else if (expectedVariable) { // get variable
                variables.put(token, new SQLToken(token, position));
                variablePositions.put(position, token);
                tokenList.add(new SQLToken(sequence.toString(), position - 1));
                tokenList.add(new SQLToken(token, position));

                expectedVariable = false;
                expectedCloseIdentifier = true;

                sequence.setLength(0);

            } else if (expectedCloseIdentifier) { // check variable closes
                expect(VARIABLE_CLOSE_IDENTIFIER, token, "Expected close bracket ']' after the variable");
                expectedCloseIdentifier = false;
            } else {
                sequence.append(token);
            }
            position++;
        }

        if (!sqlProcedure.isEmpty()) {
            tokenList.add(new SQLToken(sequence.toString(), position));
        }

        return new SQLRecord(tokenList, variables, variablePositions);
    }

    private void expect(final String symbol, final String token, final String message) {
        if (!symbol.equals(token)) {
            throw new IllegalStateException(message);
        }
    }
}
