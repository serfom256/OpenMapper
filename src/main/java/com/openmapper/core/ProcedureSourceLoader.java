package com.openmapper.core;

import com.openmapper.common.FileContentReader;
import com.openmapper.common.entity.SQLProcedure;
import com.openmapper.common.mapping.SourceMapper;
import com.openmapper.core.environment.EnvironmentProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FILE_EXTENSION;
import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FSQL_FILES_PATH;

@Component
public class ProcedureSourceLoader implements EnvironmentProcessor {

    private final FileContentReader fileContentReader;

    private final SourceMapper mapper = new SourceMapper();
    private final Environment environment;
    private final OpenMapperSQLContext context;
    private final ResourceLoader resourceLoader;

    @Autowired
    public ProcedureSourceLoader(Environment environment,
                                 FileContentReader fileContentReader,
                                 OpenMapperSQLContext context,
                                 ResourceLoader resourceLoader) {
        this.fileContentReader = fileContentReader;
        this.environment = environment;
        this.context = context;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void processEnvironment() {
        final List<String> sqlFilePaths = getFsqlPaths(FSQL_FILES_PATH.value());
        final List<SQLProcedure> parsed = fileContentReader.readFiles(sqlFilePaths);
        parsed.stream()
                .map(sqlProcedure -> new SQLProcedure(sqlProcedure.getFunctionName(), format(sqlProcedure.getFunctionBody())))
                .forEach(sqlProcedure -> context.updateContext(sqlProcedure.getFunctionName(), mapper.map(sqlProcedure.getFunctionBody())));
    }

    @SuppressWarnings("unchecked")
    private List<String> getFsqlPaths(String propertyName) {
        List<String> properties = environment.getProperty(propertyName, List.class);
        if (properties != null) return properties;
        try {
            File[] classPathFiles = resourceLoader.getResource("classpath:/").getFile().listFiles();
            if (classPathFiles == null) return Collections.emptyList();
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
