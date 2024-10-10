package com.openmapper.core.query.source.mapping;

import java.util.Map;

import com.openmapper.parser.model.SQLRecord;

public interface InputMapper {
    String mapSql(SQLRecord entity, Map<String, Object> toReplace);
}
