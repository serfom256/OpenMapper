package com.openmapper.core.impl;

import com.openmapper.core.loader.OpenMapperEnvironmentProcessor;
import com.openmapper.entity.FsqlEntity;
import com.openmapper.util.FileUtil;
import com.openmapper.util.ParsedObjectsFormatter;
import com.openmapper.core.mapping.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FSQL_FILES_PATH;

@Component
public class SqlLoader implements OpenMapperEnvironmentProcessor {

    private final FileUtil fileUtil;
    private final Environment environment;
    private final ParsedObjectsFormatter formatter;
    private final SourceMapper mapper;
    private final FsqlContext context;

    @Autowired
    public SqlLoader(Environment environment, FileUtil fileUtil, ParsedObjectsFormatter formatter, SourceMapper mapper, FsqlContext context) {
        this.fileUtil = fileUtil;
        this.environment = environment;
        this.formatter = formatter;
        this.mapper = mapper;
        this.context = context;
    }

    @Override
    public void load() {
        List<String> fsqlFiles = getFsqlFileNames(FSQL_FILES_PATH.getValue());
        Map<String, String> parsed = fileUtil.findFilesAndParse(fsqlFiles);
        formatter.format(parsed);
        Map<String, FsqlEntity> res = mapper.map(parsed);
        for (Map.Entry<String, FsqlEntity> m : res.entrySet()) {
            context.updateContext(m.getKey(), m.getValue());
        }
    }


    @SuppressWarnings("unchecked")
    private List<String> getFsqlFileNames(String propertyName) {
        return environment.getProperty(propertyName, List.class);
    }
}
