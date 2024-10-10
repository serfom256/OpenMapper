package com.openmapper.core.query.source.mapping.impl;

import org.springframework.stereotype.Component;

import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.query.source.mapping.InputWrapper;

@Component
public class InputWrapperImpl implements InputWrapper {

    private static final String STRING_WRAPPING_PATTERN = "'%s'";
    private final OpenMapperGlobalEnvironmentVariables variables;

    public InputWrapperImpl(OpenMapperGlobalEnvironmentVariables variables) {
        this.variables = variables;
    }

    @Override
    public String wrapInput(Object variable) {
        if (variables.isWrappingEnabled() && variable instanceof String) {
            return STRING_WRAPPING_PATTERN.formatted(variable);
        }
        return variable.toString();
    }

}
