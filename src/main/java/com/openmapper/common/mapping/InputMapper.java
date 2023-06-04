package com.openmapper.common.mapping;

import com.openmapper.core.entity.SQLRecord;

import java.util.Map;

public interface InputMapper {
    String mapSql(SQLRecord entity, Map<String, Object> toReplace);
}
