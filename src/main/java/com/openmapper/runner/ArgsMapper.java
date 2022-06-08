package com.openmapper.runner;

import com.openmapper.entity.FsqlEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ArgsMapper {

    public final String map(Map<String, FsqlEntity> source, String... args) {

        return null;
    }
//    public String buildFor(Objects... args) {
//        StringBuilder result = new StringBuilder();
//        for (String token : sql) {
//            if (isVariable(token)) {
//                token = formatToken(token);
//            }
//            result.append(token).append(' ');
//        }
//        return result.toString();
//    }
//
//    private String formatToken(String token) {
//        token = token.substring(1, token.length() - 1);
//        if (values.contains(token)) {
//            token = isNumeric(token) ? token : String.format(VARIABLE_FORMAT.getValue(), token);
//        }
//        return token;
//    }
//
//    private boolean isVariable(String s) {
//        return s.length() > 2 && s.startsWith("{") && s.endsWith("}");
//    }
//
//    private boolean isNumeric(String s) {
//        return s.chars().allMatch(Character::isDigit);
//    }
}
