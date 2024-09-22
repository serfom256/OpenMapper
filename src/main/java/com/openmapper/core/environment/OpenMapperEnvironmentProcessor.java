package com.openmapper.core.environment;

import com.openmapper.config.OpenMapperGlobalContext;
import com.openmapper.core.PackageScanner;
import com.openmapper.core.facade.QueryFacade;
import com.openmapper.annotations.DaoLayer;
import com.openmapper.core.QueryMappingInvocationHandler;
import com.openmapper.core.proxy.InvocationProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.PACKAGE_TO_SCAN;

@Component
public class OpenMapperEnvironmentProcessor implements EnvironmentProcessor {

    private final ConfigurableEnvironment environment;

    private final DefaultListableBeanFactory beanFactory;
    private final QueryFacade queryFacade;

    private final OpenMapperGlobalContext globalContext;

    private final PackageScanner scanner;
    private final InvocationProxy proxy = new InvocationProxy();
    private static final Logger logger = LoggerFactory.getLogger(OpenMapperEnvironmentProcessor.class);

    public OpenMapperEnvironmentProcessor(
            ConfigurableEnvironment environment,
            DefaultListableBeanFactory beanFactory,
            OpenMapperGlobalContext globalContext,
            PackageScanner scanner,
            QueryFacade queryFacade) {
        this.queryFacade = queryFacade;
        this.environment = environment;
        this.beanFactory = beanFactory;
        this.globalContext = globalContext;
        this.scanner = scanner;
    }

    @Override
    public void processEnvironment() {
        Set<Class<?>> classes = scanner.scanPackagesFor(environment.getProperty(PACKAGE_TO_SCAN.value()),
                DaoLayer.class);

        for (Class<?> clazz : classes) {
            Object dataSource = beanFactory.getBean(clazz.getAnnotation(DaoLayer.class).dataSource());
            if (globalContext.isLogging()){
                logger.info("Detected datasource: {}", dataSource);
            }
            Object proxiedClass = proxy.makeProxyFor(new QueryMappingInvocationHandler(queryFacade), clazz.getName());

            registerProxyClass(proxiedClass, clazz);
        }
    }

    private void registerProxyClass(Object proxy, Class<?> clazz){
        if (proxy == null) {
            throw new IllegalStateException(clazz.getName() + " not found");
        }
        try {
            beanFactory.registerSingleton(clazz.getSimpleName(), proxy);
            if (globalContext.isLogging())
                logger.debug("Class: '{}' registered", clazz.getName());
        } catch (Exception ignored) {
            logger.warn("Proxy registered with warnings");
        }
    }
}
