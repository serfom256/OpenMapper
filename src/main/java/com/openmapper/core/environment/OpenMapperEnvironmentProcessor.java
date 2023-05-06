package com.openmapper.core.environment;

import com.openmapper.config.OpenMapperGlobalContext;
import com.openmapper.core.annotations.DaoLayer;
import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.files.mapping.InputMapper;
import com.openmapper.core.files.mapping.InputMapperImpl;
import com.openmapper.core.proxy.EntityMappingInvocationHandler;
import com.openmapper.core.proxy.InvocationProxy;
import com.openmapper.core.query.QueryExecutorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Set;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.PACKAGE_TO_SCAN;

@Component
public class OpenMapperEnvironmentProcessor implements EnvironmentProcessor {

    private final ConfigurableEnvironment environment;
    private final OpenMapperSqlContext context;

    private final DefaultListableBeanFactory beanFactory;
    private final QueryExecutorStrategy strategy;

    private final OpenMapperGlobalContext globalContext;


    private final PackageScanner scanner = new PackageScanner();
    private final InvocationProxy proxy = new InvocationProxy();
    private static final Logger logger = LoggerFactory.getLogger(OpenMapperEnvironmentProcessor.class);

    @Autowired
    public OpenMapperEnvironmentProcessor(
            ConfigurableEnvironment environment,
            OpenMapperSqlContext context,
            DefaultListableBeanFactory beanFactory,
            QueryExecutorStrategy strategy,
            OpenMapperGlobalContext globalContext) {
        this.environment = environment;
        this.context = context;
        this.beanFactory = beanFactory;
        this.strategy = strategy;
        this.globalContext = globalContext;
    }

    @Override
    public void processEnvironment() {
        if (globalContext.isLogging()) {
            logger.info("Scanning: {}", environment.getRequiredProperty(PACKAGE_TO_SCAN.getValue()));
        }
        Set<Class<?>> classes = scanner.scanPackagesFor(environment.getProperty(PACKAGE_TO_SCAN.getValue()), DaoLayer.class);
        for (Class<?> clazz : classes) {
            Object dataSource = beanFactory.getBean(clazz.getAnnotation(DaoLayer.class).dataSource());
            if (globalContext.isLogging()) logger.info("Detected datasource: {}", dataSource);
            Object proxiedClass = proxy.makeProxyFor(new EntityMappingInvocationHandler(context, strategy, createInputMapper(), (DataSource) dataSource), clazz.getName());
            if (proxiedClass == null) {
                throw new IllegalStateException(clazz.getName() + " not found");
            }
            try {
                beanFactory.registerSingleton(clazz.getSimpleName(), proxiedClass);
                if (globalContext.isLogging()) logger.debug("Class: '{}' registered", clazz.getName());
            } catch (Exception ignored) {
                logger.warn("Proxy registered with warnings");
            }
        }
    }

    private InputMapper createInputMapper() {
        return new InputMapperImpl(globalContext.isSqlTracing());
    }
}
