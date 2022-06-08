package com.openmapper.runner;

import com.openmapper.entity.FsqlEntity;
import com.openmapper.util.FileUtil;
import com.openmapper.util.ParsedObjectsFormatter;
import com.openmapper.util.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class Runner {

    private final FileUtil util;

    private final SourceMapper mapper;

    private final ParsedObjectsFormatter formatter;

    @Autowired
    public Runner(FileUtil util, ParsedObjectsFormatter formatter) {
        this.util = util;
        this.formatter = formatter;
        this.mapper = new SourceMapper();
    }

    @PostConstruct
    public void start() {
        Map<String, String> parsed = util.findFilesAndParse();
        formatter.format(parsed);
        Map<String, FsqlEntity> res = mapper.map(parsed);
        for (Map.Entry<String, FsqlEntity> m : res.entrySet()) {
            System.out.println(m.getKey() + " " + m.getValue().getSql() + " " + m.getValue().getVariables());
        }
    }
}
