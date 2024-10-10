package com.openmapper.core.environment.processers;

import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.annotations.DaoLayer;
import com.openmapper.core.environment.EnvironmentProcessor;
import com.openmapper.core.environment.PackageScanner;
import com.openmapper.core.proxy.InvocationProxy;
import com.openmapper.core.proxy.QueryMappingInvocationHandler;
import com.openmapper.core.query.facade.QueryFacade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

import javax.sql.DataSource;

@Component
public class DaoEnvironmentProcessor implements EnvironmentProcessor {

    private final OpenMapperGlobalEnvironmentVariables variables;

    private final DefaultListableBeanFactory beanFactory;
    private final QueryFacade queryFacade;

    private final PackageScanner scanner;
    private final InvocationProxy proxy = new InvocationProxy();
    private static final Logger logger = LoggerFactory.getLogger(DaoEnvironmentProcessor.class);

    public DaoEnvironmentProcessor(
            OpenMapperGlobalEnvironmentVariables variables,
            DefaultListableBeanFactory beanFactory,
            PackageScanner scanner,
            QueryFacade queryFacade) {
        this.queryFacade = queryFacade;
        this.beanFactory = beanFactory;
        this.scanner = scanner;
        this.variables = variables;
    }

    @Override
    public void processEnvironment() {
        Set<Class<?>> classes = scanner.scanPackagesFor(variables.getDaoPackageToScan(), DaoLayer.class);

        for (Class<?> clazz : classes) {
            DataSource dataSource = beanFactory.getBean(DataSource.class,
                    clazz.getAnnotation(DaoLayer.class).dataSource());
            if (variables.isLoggingEnabled()) {
                logger.info("Detected datasource: {}", dataSource);
            }

            final var queryHandler = new QueryMappingInvocationHandler(queryFacade, dataSource);
            Object proxiedClass = proxy.makeProxyFor(queryHandler, clazz.getName());

            registerProxyClass(proxiedClass, clazz);
        }
    }

    private void registerProxyClass(Object proxy, Class<?> clazz) {
        if (proxy == null) {
            throw new IllegalStateException(clazz.getName() + " not found");
        }
        try {
            beanFactory.registerSingleton(clazz.getSimpleName(), proxy);
            if (variables.isLoggingEnabled()) {
                logger.debug("Class: '{}' registered", clazz.getName());
            }
        } catch (Exception ignored) {
            logger.warn("Proxy registered with warnings");
        }
    }
}
