package io.github.serfom256.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ResourceLoader;

import io.github.serfom256.common.FileContentReader;
import io.github.serfom256.core.common.DaoMethodFacade;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.context.ModelMetadataContext;
import io.github.serfom256.core.context.OpenMapperSQLContext;
import io.github.serfom256.core.environment.PackageScanner;
import io.github.serfom256.core.environment.processors.DaoMethodEnvironmentProcessor;
import io.github.serfom256.core.environment.processors.ModelEnvironmentProcessor;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.core.environment.processors.ProcedureSourceEnvironmentProcessor;
import io.github.serfom256.parser.SourceMapper;
import io.github.serfom256.parser.factory.DefaultParserFactory;

/**
 * Startup-time machinery: the metadata contexts, the classpath scanner, and the
 * {@link io.github.serfom256.core.environment.EnvironmentProcessor}s that populate
 * those contexts before any DAO is invoked.
 *
 * <p>The {@code @Order} values on the processors are deliberate. They reproduce
 * the sequence component scanning used to produce implicitly, and pin it: the
 * processors share mutable context beans, so their relative order is behaviour,
 * not a detail.
 */
@Configuration(proxyBeanMethods = false)
public class OpenMapperEnvironmentConfiguration {

    @Bean
    public DaoMethodContext daoMethodContext() {
        return new DaoMethodContext();
    }

    @Bean
    public ModelMetadataContext modelMetadataContext() {
        return new ModelMetadataContext();
    }

    @Bean
    public OpenMapperSQLContext openMapperSQLContext(DaoMethodFacade daoMethodFacade) {
        return new OpenMapperSQLContext(daoMethodFacade);
    }

    @Bean
    public DaoMethodFacade daoMethodFacade(DaoMethodContext daoMethodContext, SourceMapper mapper) {
        return new DaoMethodFacade(daoMethodContext, mapper);
    }

    @Bean
    public ModelSpecificationReader modelSpecificationReader() {
        return new ModelSpecificationReader();
    }

    @Bean
    public SourceMapper sourceMapper() {
        return new SourceMapper();
    }

    @Bean
    public DefaultParserFactory defaultParserFactory() {
        return new DefaultParserFactory();
    }

    @Bean
    public FileContentReader fileContentReader(OpenMapperGlobalEnvironmentVariables variables,
                                               DefaultParserFactory parserFactory) {
        return new FileContentReader(variables, parserFactory);
    }

    @Bean
    public PackageScanner packageScanner(OpenMapperGlobalEnvironmentVariables variables,
                                         ApplicationContext context) {
        return new PackageScanner(variables, context);
    }

    @Bean
    @Order(2)
    public DaoMethodEnvironmentProcessor daoMethodEnvironmentProcessor(PackageScanner scanner,
                                                                       OpenMapperGlobalEnvironmentVariables variables,
                                                                       DaoMethodContext daoMethodContext) {
        return new DaoMethodEnvironmentProcessor(scanner, variables, daoMethodContext);
    }

    @Bean
    @Order(3)
    public ModelEnvironmentProcessor modelEnvironmentProcessor(PackageScanner scanner,
                                                               OpenMapperGlobalEnvironmentVariables variables,
                                                               ModelSpecificationReader modelSpecificationReader,
                                                               ModelMetadataContext modelMetadataContext,
                                                               MappingPlanRegistry mappingPlanRegistry) {
        return new ModelEnvironmentProcessor(scanner, variables, modelSpecificationReader, modelMetadataContext,
                mappingPlanRegistry);
    }

    @Bean
    @Order(4)
    public ProcedureSourceEnvironmentProcessor procedureSourceEnvironmentProcessor(SourceMapper mapper,
                                                                                   FileContentReader fileContentReader,
                                                                                   OpenMapperGlobalEnvironmentVariables variables,
                                                                                   OpenMapperSQLContext context,
                                                                                   ResourceLoader resourceLoader) {
        return new ProcedureSourceEnvironmentProcessor(mapper, fileContentReader, variables, context, resourceLoader);
    }
}
