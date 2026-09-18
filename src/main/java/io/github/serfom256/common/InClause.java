package io.github.serfom256.common;

import org.jspecify.annotations.Nullable;

record InClause(int operandStart, String operand, boolean negated) {

    static @Nullable InClause endingAt(StringBuilder sql) {
        int i = skipWhitespaceBack(sql, sql.length());
        if (i == 0 || sql.charAt(i - 1) != '(') {
            return null;
        }
        i = skipWhitespaceBack(sql, i - 1);
        i = wordBack(sql, i, "IN");
        if (i < 0) {
            return null;
        }
        int afterIn = skipWhitespaceBack(sql, i);
        int afterNot = wordBack(sql, afterIn, "NOT");
        boolean negated = afterNot >= 0;
        int operandEnd = skipWhitespaceBack(sql, negated ? afterNot : afterIn);
        int operandStart = operandBack(sql, operandEnd);
        if (operandStart < 0 || operandStart == operandEnd) {
            return null;
        }
        return new InClause(operandStart, sql.substring(operandStart, operandEnd), negated);
    }

    String chunkSeparator() {
        return negated ? ") AND %s NOT IN (".formatted(operand) : ") OR %s IN (".formatted(operand);
    }

    String emptyPredicate() {
        return negated ? "1 = 1" : "1 = 0";
    }

    private static int skipWhitespaceBack(CharSequence sql, int end) {
        int i = end;
        while (i > 0 && Character.isWhitespace(sql.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    private static int wordBack(CharSequence sql, int end, String word) {
        int start = end - word.length();
        if (start < 0) {
            return -1;
        }
        for (int i = 0; i < word.length(); i++) {
            if (Character.toUpperCase(sql.charAt(start + i)) != word.charAt(i)) {
                return -1;
            }
        }
        if (start > 0 && isOperandChar(sql.charAt(start - 1))) {
            return -1;
        }
        return start;
    }

    private static int operandBack(CharSequence sql, int end) {
        int i = end;
        int depth = 0;
        while (i > 0) {
            char c = sql.charAt(i - 1);
            if (c == ')') {
                depth++;
            } else if (c == '(') {
                if (depth == 0) {
                    break;
                }
                depth--;
            } else if (depth == 0 && !isOperandChar(c)) {
                break;
            }
            i--;
        }
        return i;
    }

    private static boolean isOperandChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '"' || c == '`' || c == '$';
    }
}
