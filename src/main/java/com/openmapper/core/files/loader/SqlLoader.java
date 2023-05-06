package com.openmapper.core.files.loader;

import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.environment.EnvironmentProcessor;
import com.openmapper.core.entity.FsqlEntity;
import com.openmapper.core.files.FileUtil;
import com.openmapper.common.ParsedObjectsFormatter;
import com.openmapper.core.files.mapping.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.FSQL_FILES_PATH;

@Component
public class SqlLoader implements EnvironmentProcessor {

    private final FileUtil fileUtil;
    private final Environment environment;
    private final ParsedObjectsFormatter formatter;
    private final SourceMapper mapper;
    private final OpenMapperSqlContext context;

    @Autowired
    public SqlLoader(Environment environment, FileUtil fileUtil, ParsedObjectsFormatter formatter, SourceMapper mapper, OpenMapperSqlContext context) {
        this.fileUtil = fileUtil;
        this.environment = environment;
        this.formatter = formatter;
        this.mapper = mapper;
        this.context = context;
    }

    @Override
    public void processEnvironment() {
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
