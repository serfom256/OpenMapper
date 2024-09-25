package com.openmapper.core;

import com.openmapper.common.FileContentReader;
import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.common.mapping.SourceMapper;
import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.environment.EnvironmentProcessor;
import com.openmapper.exceptions.fsql.InvalidDeclarationException;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.openmapper.config.OpenMapperGlobalConstants.FILE_EXTENSION;

@Component
public class ProcedureSourceLoader implements EnvironmentProcessor {

    private final FileContentReader fileContentReader;
    private final OpenMapperGlobalEnvironmentVariables variables;

    private final SourceMapper mapper = new SourceMapper();
    private final OpenMapperSQLContext context;
    private final ResourceLoader resourceLoader;

    public ProcedureSourceLoader(
            OpenMapperGlobalEnvironmentVariables variables,
            FileContentReader fileContentReader,
            OpenMapperSQLContext context,
            ResourceLoader resourceLoader) {
        this.fileContentReader = fileContentReader;
        this.context = context;
        this.resourceLoader = resourceLoader;
        this.variables = variables;
    }

    @Override
    public void processEnvironment() {
        final List<String> sqlFilePaths = getPaths();
        final List<SQLProcedure> parsed = fileContentReader.readFiles(sqlFilePaths);
        parsed.stream()
                .map(sqlProcedure -> new SQLProcedure(sqlProcedure.getFunctionName(),
                        format(sqlProcedure.getFunctionBody())))

                .forEach(sqlProcedure -> {
                    try {
                        context.updateContext(sqlProcedure.getFunctionName(),
                                mapper.map(sqlProcedure.getFunctionBody()));
                    } catch (IllegalStateException e) {
                        throw new InvalidDeclarationException(sqlProcedure.getFunctionName(), e.getMessage());
                    }
                });
    }

    private List<String> getPaths() {
        List<String> properties = variables.getSqlFilePaths();
        if (properties != null)
            return properties;
        try {
            File[] classPathFiles = resourceLoader.getResource("classpath:/").getFile().listFiles();
            if (classPathFiles == null)
                return Collections.emptyList();
            return Arrays.stream(classPathFiles).filter(f -> f.getName()
                    .endsWith(FILE_EXTENSION.value()))
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public String format(final String procedureBody) {
        return procedureBody.replaceAll("\\s+", " ").trim();
    }
}
