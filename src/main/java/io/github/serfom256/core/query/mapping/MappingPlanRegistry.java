package io.github.serfom256.core.query.mapping;

import io.github.serfom256.json.Serializer;
import io.github.serfom256.mappers.ColumnTypeRegistry;

import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lazily builds and caches one {@link MappingPlan} per entity type. Plans are
 * immutable and reused across all queries, so each type is compiled at most once.
 */
public class MappingPlanRegistry {

    private final Map<Class<?>, MappingPlan> plans = new ConcurrentHashMap<>();
    private final MappingPlanBuilder builder;

    public MappingPlanRegistry(@Nullable Serializer jsonCodec) {
        this(jsonCodec, new ColumnTypeRegistry());
    }

    public MappingPlanRegistry(@Nullable Serializer jsonCodec, ColumnTypeRegistry columnTypes) {
        this.builder = new MappingPlanBuilder(jsonCodec, columnTypes);
    }

    public MappingPlan planFor(Class<?> type) {
        return plans.computeIfAbsent(type, builder::build);
    }
}
