package com.openmapper.core.resources.loader;

import com.openmapper.common.ParsedObjectsFormatter;
import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.environment.EnvironmentProcessor;
import com.openmapper.core.resources.FileUtil;
import com.openmapper.core.resources.mapping.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FILE_EXTENSION;
import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FSQL_FILES_PATH;

@Component
public class SqlLoader implements EnvironmentProcessor {

    private final FileUtil fileUtil;
    private final Environment environment;
    private final ParsedObjectsFormatter formatter;
    private final SourceMapper mapper;
    private final OpenMapperSqlContext context;
    private final ResourceLoader resourceLoader;

    @Autowired
    public SqlLoader(Environment environment,
                     FileUtil fileUtil,
                     ParsedObjectsFormatter formatter,
                     SourceMapper mapper,
                     OpenMapperSqlContext context,
                     ResourceLoader resourceLoader) {
        this.fileUtil = fileUtil;
        this.environment = environment;
        this.formatter = formatter;
        this.mapper = mapper;
        this.context = context;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void processEnvironment() {
        List<String> sqlFilePaths = getFsqlPaths(FSQL_FILES_PATH.value());
        Map<String, String> parsed = fileUtil.findFilesAndParse(sqlFilePaths);
        formatter.format(parsed);
        Map<String, FsqlEntity> res = mapper.map(parsed);
        for (Map.Entry<String, FsqlEntity> m : res.entrySet()) {
            context.updateContext(m.getKey(), m.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getFsqlPaths(String propertyName) {
        List<String> properties = environment.getProperty(propertyName, List.class);
        if (properties != null) return properties;
        try {
            File[] classPathFiles = resourceLoader.getResource("classpath:/").getFile().listFiles();
            if (classPathFiles == null) return Collections.emptyList();
            return Arrays.stream(classPathFiles).filter(f -> f.getName().endsWith(FILE_EXTENSION.value())).map(File::getAbsolutePath).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
