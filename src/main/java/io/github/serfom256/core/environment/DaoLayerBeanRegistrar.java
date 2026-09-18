package io.github.serfom256.core.environment;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.proxy.InvocationProxy;
import io.github.serfom256.core.proxy.QueryMappingInvocationHandler;
import io.github.serfom256.core.query.facade.QueryFacade;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import org.jspecify.annotations.Nullable;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

/**
 * Registers a bean definition for every {@code @DaoLayer} interface before any
 * application bean is instantiated.
 *
 * <p>Registration happens at bean-definition time rather than while the context
 * is already building beans, so a DAO can be injected into any bean regardless
 * of the order the container happens to create them in. The proxy itself is
 * built lazily by the definition's instance supplier, once
 * {@link QueryFacade} and the {@link DataSource} are available.
 */
public class DaoLayerBeanRegistrar implements BeanDefinitionRegistryPostProcessor, EnvironmentAware, Ordered {

    private static final String NO_ROOT_PACKAGE =
            "openMapper found no package to scan: there is no @SpringBootApplication bean definition"
                    + " and openmapper.dao.packagesToScan is not set. No DAO was registered.";

    private static final Logger logger = LoggerFactory.getLogger(DaoLayerBeanRegistrar.class);

    private static final String ENVIRONMENT_PROCESSOR = "openMapperEnvironmentProcessor";

    private final Map<String, Class<?>> registered = new HashMap<>();

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        if (!(registry instanceof ConfigurableListableBeanFactory beanFactory)) {
            return;
        }
        OpenMapperGlobalEnvironmentVariables variables = new OpenMapperGlobalEnvironmentVariables(environment);
        String packageToScan = variables.getDaoPackageToScan();
        if (packageToScan == null) {
            packageToScan = rootPackageOf(registry);
        }
        if (packageToScan == null) {
            logger.warn(NO_ROOT_PACKAGE);
            return;
        }

        Set<Class<?>> daoInterfaces = new Reflections(packageToScan).getTypesAnnotatedWith(DaoLayer.class);
        for (Class<?> daoInterface : daoInterfaces) {
            register(registry, beanFactory, daoInterface);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    private void register(BeanDefinitionRegistry registry,
                          ConfigurableListableBeanFactory beanFactory,
                          Class<?> daoInterface) {
        String beanName = Introspector.decapitalize(daoInterface.getSimpleName());
        Class<?> clash = registered.put(beanName, daoInterface);
        if (clash != null && clash != daoInterface) {
            throw new IllegalStateException(
                    "Two @DaoLayer interfaces share the bean name '%s': %s and %s. Rename one of them."
                            .formatted(beanName, clash.getName(), daoInterface.getName()));
        }
        if (registry.containsBeanDefinition(beanName)) {
            return;
        }
        RootBeanDefinition definition = new RootBeanDefinition(daoInterface);
        definition.setTargetType(daoInterface);
        definition.setInstanceSupplier(() -> createProxy(beanFactory, daoInterface));
        registry.registerBeanDefinition(beanName, definition);
    }

    private Object createProxy(ConfigurableListableBeanFactory beanFactory, Class<?> daoInterface) {
        beanFactory.getBean(ENVIRONMENT_PROCESSOR);
        QueryFacade queryFacade = beanFactory.getBean(QueryFacade.class);
        DataSource dataSource = resolveDataSource(beanFactory, daoInterface.getAnnotation(DaoLayer.class).dataSource());
        QueryMappingInvocationHandler handler = new QueryMappingInvocationHandler(queryFacade, dataSource);
        return new InvocationProxy().makeProxyFor(handler, daoInterface.getName());
    }

    private DataSource resolveDataSource(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (!beanName.isEmpty() && beanFactory.containsBean(beanName)) {
            return beanFactory.getBean(beanName, DataSource.class);
        }
        return beanFactory.getBean(DataSource.class);
    }

    private @Nullable String rootPackageOf(BeanDefinitionRegistry registry) {
        for (String name : registry.getBeanDefinitionNames()) {
            BeanDefinition definition = registry.getBeanDefinition(name);
            if (definition instanceof AnnotatedBeanDefinition annotated
                    && annotated.getMetadata().hasAnnotation(
                            "org.springframework.boot.autoconfigure.SpringBootApplication")) {
                String className = annotated.getMetadata().getClassName();
                int lastDot = className.lastIndexOf('.');
                if (lastDot > 0) {
                    return className.substring(0, lastDot);
                }
            }
        }
        return null;
    }
}
