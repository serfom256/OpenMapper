package com.openmapper.core.files.mapping;

import com.openmapper.core.entity.FsqlEntity;

import java.util.Map;

public interface InputMapper {
    String mapSql(FsqlEntity entity, Map<String, Object> toReplace);
}
