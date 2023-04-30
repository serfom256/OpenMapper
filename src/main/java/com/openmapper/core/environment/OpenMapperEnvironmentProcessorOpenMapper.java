package com.openmapper.core.environment;

import com.openmapper.core.annotations.DaoLayer;
import com.openmapper.core.OpenMapperSqlContext;
import com.openmapper.core.proxy.EntityMappingInvocationHandler;
import com.openmapper.core.proxy.InvocationProxy;
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
public class OpenMapperEnvironmentProcessorOpenMapper implements OpenMapperEnvironmentProcessor {

    private final ConfigurableEnvironment environment;
    private final OpenMapperSqlContext context;
    private final PackageScanner scanner;
    private final InvocationProxy proxy;
    private final DefaultListableBeanFactory beanFactory;

    private static final Logger logger = LoggerFactory.getLogger(OpenMapperEnvironmentProcessorOpenMapper.class);

    @Autowired
    public OpenMapperEnvironmentProcessorOpenMapper(ConfigurableEnvironment environment, OpenMapperSqlContext context, DefaultListableBeanFactory beanFactory) {
        this.environment = environment;
        this.context = context;
        this.beanFactory = beanFactory;
        this.scanner = new PackageScanner();
        this.proxy = new InvocationProxy();
    }

    @Override
    public void processEnvironment() { // todo move to BeanDefinitionRegistryPostProcessor
        logger.info("Scanning: ".concat(environment.getRequiredProperty(PACKAGE_TO_SCAN.getValue())));
        Set<Class<?>> classes = scanner.scanPackagesFor(environment.getProperty(PACKAGE_TO_SCAN.getValue()), DaoLayer.class);
        for (Class<?> clazz : classes) { // todo handle sql loaders and sql mappers separately
            Object dataSource = beanFactory.getBean(clazz.getAnnotation(DaoLayer.class).dataSource());
            System.out.println(dataSource);
            Object proxiedClass = proxy.makeProxyFor(new EntityMappingInvocationHandler(context, environment, (DataSource) dataSource), clazz.getName());
            if (proxiedClass == null) {
                throw new IllegalStateException(clazz.getName() + " not found");
            }
            logger.debug(clazz.getSimpleName());
            try {
                beanFactory.registerSingleton(clazz.getSimpleName(), proxiedClass);
            } catch (Exception ignored) {
                logger.warn("Proxy registered with warnings");
            }
        }
    }
}
