package io.github.serfom256.common;

import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.exceptions.fsql.InvalidFileFormatException;
import io.github.serfom256.parser.factory.DefaultParserFactory;
import io.github.serfom256.parser.model.SQLProcedure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and parses {@code .fsql} resources. Resources are read through their
 * {@link InputStream}, so this works whether the file lives on the filesystem or
 * inside a jar.
 */
public class FileContentReader {

    private final OpenMapperGlobalEnvironmentVariables variables;
    private final DefaultParserFactory parserFactory;
    private static final Logger logger = LoggerFactory.getLogger(FileContentReader.class);

    public FileContentReader(OpenMapperGlobalEnvironmentVariables variables, DefaultParserFactory parserFactory) {
        this.variables = variables;
        this.parserFactory = parserFactory;
    }

    public List<SQLProcedure> readResources(final List<Resource> resources) {
        final List<SQLProcedure> procedures = new ArrayList<>();
        for (Resource resource : resources) {
            if (variables.isLoggingEnabled()) {
                logger.debug("Loading fsql resource: {}", resource.getDescription());
            }
            procedures.addAll(parse(resource));
        }
        return procedures;
    }

    private List<SQLProcedure> parse(Resource resource) {
        String name = resource.getFilename() != null ? resource.getFilename() : resource.getDescription();
        try (InputStream in = resource.getInputStream()) {
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return parserFactory.getParser(name, content).parse();
        } catch (IOException e) {
            throw new InvalidFileFormatException(name);
        }
    }
}
