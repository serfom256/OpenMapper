package com.openmapper.core.environment;

import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.PackageScanner;
import com.openmapper.core.facade.QueryFacade;
import com.openmapper.annotations.DaoLayer;
import com.openmapper.core.QueryMappingInvocationHandler;
import com.openmapper.core.proxy.InvocationProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OpenMapperEnvironmentProcessor implements EnvironmentProcessor {

    private final OpenMapperGlobalEnvironmentVariables variables;

    private final DefaultListableBeanFactory beanFactory;
    private final QueryFacade queryFacade;

    private final PackageScanner scanner;
    private final InvocationProxy proxy = new InvocationProxy();
    private static final Logger logger = LoggerFactory.getLogger(OpenMapperEnvironmentProcessor.class);

    public OpenMapperEnvironmentProcessor(
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
        Set<Class<?>> classes = scanner.scanPackagesFor(variables.getPackageToScan(), DaoLayer.class);

        for (Class<?> clazz : classes) {
            Object dataSource = beanFactory.getBean(clazz.getAnnotation(DaoLayer.class).dataSource());
            if (variables.isLogging()) {
                logger.info("Detected datasource: {}", dataSource);
            }
            Object proxiedClass = proxy.makeProxyFor(new QueryMappingInvocationHandler(queryFacade), clazz.getName());

            registerProxyClass(proxiedClass, clazz);
        }
    }

    private void registerProxyClass(Object proxy, Class<?> clazz) {
        if (proxy == null) {
            throw new IllegalStateException(clazz.getName() + " not found");
        }
        try {
            beanFactory.registerSingleton(clazz.getSimpleName(), proxy);
            if (variables.isLogging())
                logger.debug("Class: '{}' registered", clazz.getName());
        } catch (Exception ignored) {
            logger.warn("Proxy registered with warnings");
        }
    }
}
