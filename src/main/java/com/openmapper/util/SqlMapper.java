package com.openmapper.util;

import com.openmapper.entity.FsqlEntity;

import java.util.Map;

public interface SqlMapper {
    String mapSql(FsqlEntity entity, Map<String, Object> toReplace);
}
