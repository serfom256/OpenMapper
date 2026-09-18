package io.github.serfom256.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.serfom256.common.reflect.FieldValueGenerator;
import io.github.serfom256.common.reflect.ModelFieldReader;
import io.github.serfom256.common.reflect.ModelPropertyExtractor;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.query.mapping.ResultSetGraphReader;
import io.github.serfom256.json.Serializer;
import io.github.serfom256.core.lifecycle.EntityWriteListener;
import io.github.serfom256.core.lifecycle.EntityIdGenerators;
import io.github.serfom256.core.lifecycle.EntityWriteListeners;
import io.github.serfom256.core.lifecycle.IdGenerator;
import io.github.serfom256.mappers.ColumnTypeConverter;
import io.github.serfom256.mappers.ColumnTypeRegistry;
import io.github.serfom256.mappers.ModelExtractor;
import io.github.serfom256.mappers.ResultSetObjectMapper;
import io.github.serfom256.mappers.ResultSetPrimitiveMapper;

/**
 * Result-set to model mapping, and the reflective helpers behind it.
 *
 * <p>{@link Serializer} is optional: it exists only when a Jackson codec is on
 * the classpath, so it is resolved through an {@link ObjectProvider} rather than
 * declared as a hard dependency.
 */
@Configuration(proxyBeanMethods = false)
public class OpenMapperMappingConfiguration {

    @Bean
    public FieldValueGenerator fieldValueGenerator() {
        return new FieldValueGenerator();
    }

    @Bean
    public ColumnTypeRegistry columnTypeRegistry(ObjectProvider<ColumnTypeConverter<?>> converters) {
        return new ColumnTypeRegistry(converters.orderedStream().toList());
    }

    @Bean
    public ModelFieldReader modelFieldReader() {
        return new ModelFieldReader();
    }

    @Bean
    public ModelPropertyExtractor modelPropertyExtractor(ModelExtractor modelExtractor,
                                                         EntityWriteListeners entityWriteListeners,
                                                         EntityIdGenerators entityIdGenerators) {
        return new ModelPropertyExtractor(modelExtractor, entityWriteListeners, entityIdGenerators);
    }

    @Bean
    public EntityIdGenerators entityIdGenerators(ObjectProvider<IdGenerator<?>> generators,
                                                 ModelFieldReader modelFieldReader) {
        return new EntityIdGenerators(generators.orderedStream().toList(), modelFieldReader);
    }

    @Bean
    public EntityWriteListeners entityWriteListeners(ObjectProvider<EntityWriteListener<?>> listeners) {
        return new EntityWriteListeners(listeners.orderedStream().toList());
    }

    @Bean
    public ModelExtractor modelExtractor(FieldValueGenerator fieldValueGenerator,
                                         ModelMetadataContext modelMetadataContext,
                                         ObjectProvider<Serializer> jsonCodec,
                                         ModelFieldReader modelFieldReader,
                                         ColumnTypeRegistry columnTypeRegistry) {
        return new ModelExtractor(fieldValueGenerator, modelMetadataContext, jsonCodec.getIfAvailable(),
                modelFieldReader, columnTypeRegistry);
    }

    @Bean
    public MappingPlanRegistry mappingPlanRegistry(ObjectProvider<Serializer> jsonCodec,
                                                   ColumnTypeRegistry columnTypeRegistry) {
        return new MappingPlanRegistry(jsonCodec.getIfAvailable(), columnTypeRegistry);
    }

    @Bean
    public ResultSetGraphReader resultSetGraphReader(MappingPlanRegistry registry) {
        return new ResultSetGraphReader(registry);
    }

    @Bean
    public ResultSetObjectMapper resultSetObjectMapper(ResultSetGraphReader graphReader) {
        return new ResultSetObjectMapper(graphReader);
    }

    @Bean
    public ResultSetPrimitiveMapper resultSetPrimitiveMapper() {
        return new ResultSetPrimitiveMapper();
    }
}
