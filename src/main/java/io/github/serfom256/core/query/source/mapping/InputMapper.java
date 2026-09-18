package io.github.serfom256.core.query.source.mapping;

import java.util.Map;

import io.github.serfom256.core.query.model.BoundSql;
import io.github.serfom256.parser.model.SQLRecord;

public interface InputMapper {
    BoundSql mapSql(SQLRecord entity, Map<String, Object> toReplace);
}
