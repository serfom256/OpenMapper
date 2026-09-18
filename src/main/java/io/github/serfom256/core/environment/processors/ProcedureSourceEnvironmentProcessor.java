package io.github.serfom256.core.environment.processors;

import io.github.serfom256.common.FileContentReader;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.environment.EnvironmentProcessor;
import io.github.serfom256.exceptions.fsql.InvalidDeclarationException;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.model.SQLProcedure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.serfom256.config.OpenMapperGlobalConstants.FILE_EXTENSION;

public class ProcedureSourceEnvironmentProcessor implements EnvironmentProcessor {

    private final SourceMapper mapper;
    private final FileContentReader fileContentReader;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final OpenMapperSQLContext context;
    private final ResourcePatternResolver resourceResolver;

    private static final Logger logger = LoggerFactory.getLogger(ProcedureSourceEnvironmentProcessor.class);

    public ProcedureSourceEnvironmentProcessor(
            SourceMapper mapper,
            FileContentReader fileContentReader,
            OpenMapperGlobalEnvironmentVariables variables,
            OpenMapperSQLContext context,
            ResourceLoader resourceLoader) {
        this.mapper = mapper;
        this.fileContentReader = fileContentReader;
        this.variables = variables;
        this.context = context;
        this.resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
    }

    @Override
    public void processEnvironment() {
        final List<SQLProcedure> parsed = fileContentReader.readResources(resolveResources());
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

    private List<Resource> resolveResources() {
        final List<String> locations = variables.getSqlFilePaths();
        final List<String> patterns = locations == null
                ? List.of("classpath*:**/*" + FILE_EXTENSION.value())
                : locations.stream().map(this::toPattern).collect(Collectors.toList());

        final List<Resource> resources = new ArrayList<>();
        for (String pattern : patterns) {
            try {
                Collections.addAll(resources, resourceResolver.getResources(pattern));
            } catch (IOException e) {
                logger.warn("Could not resolve fsql resources for pattern '{}': {}", pattern, e.getMessage());
            }
        }
        return resources;
    }

    private String toPattern(String location) {
        String base = location.contains(":") ? location : "file:" + location;
        return location.endsWith(FILE_EXTENSION.value())
                ? base
                : base + "/**/*" + FILE_EXTENSION.value();
    }

    public String format(final String procedureBody) {
        return procedureBody.replaceAll("\\s+", " ").trim();
    }
}
