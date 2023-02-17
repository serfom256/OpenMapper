package com.openmapper.core.loader;

import com.openmapper.core.annotations.SqlGenerator;
import com.openmapper.core.impl.FsqlContext;
import com.openmapper.core.proxy.InvocationProxy;
import com.openmapper.core.proxy.SqlInvocationHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.openmapper.config.OPEN_MAPPER_CONSTANTS.PACKAGE_TO_SCAN;

@Log4j2
@Component
public class OpenMapperEnvironmentLoader implements ComponentLoader {

    private final ConfigurableEnvironment environment;
    private final FsqlContext context;
    private final PackageScanner scanner;
    private final InvocationProxy proxy;
    private final DefaultListableBeanFactory beanFactory;

    @Autowired
    public OpenMapperEnvironmentLoader(ConfigurableEnvironment environment, FsqlContext context, DefaultListableBeanFactory beanFactory) {
        this.environment = environment;
        this.context = context;
        this.beanFactory = beanFactory;
        this.scanner = new PackageScanner();
        proxy = new InvocationProxy();
    }

    @Override
    public void load() {
        log.info("scanning: " + environment.getProperty(PACKAGE_TO_SCAN.getValue()));
        Set<Class<?>> classes = scanner.scanPackagesFor(environment.getProperty(PACKAGE_TO_SCAN.getValue()), SqlGenerator.class);
        for (Class<?> clazz : classes) {
            Object proxiedClass = proxy.makeProxyFor(new SqlInvocationHandler(context), clazz.getName());
            if (proxiedClass == null) {
                throw new IllegalStateException(clazz.getName() + " not found");
            }
            log.debug(clazz.getSimpleName());
            try {
                beanFactory.registerSingleton(clazz.getSimpleName(), proxiedClass);
            } catch (Exception ignored) {
                log.warn("Proxy registered with warnings");
            }
        }
    }
}
