package io.github.serfom256.core.environment;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

final class AnnotatedTypeScanner {
    private final Environment environment;
    private final ClassLoader classLoader;

    AnnotatedTypeScanner(Environment environment, ClassLoader classLoader) {
        this.environment = environment != null ? environment : new StandardEnvironment();
        this.classLoader = classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader();
    }

    Set<Class<?>> scan(String packages, Class<? extends Annotation> annotation) {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false, environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition definition) {
                return definition.getMetadata().isIndependent();
            }
        };
        provider.setResourceLoader(new org.springframework.core.io.DefaultResourceLoader(classLoader));
        provider.addIncludeFilter(new AnnotationTypeFilter(annotation, true, true));

        Set<Class<?>> types = new LinkedHashSet<>();
        for (String basePackage : StringUtils.tokenizeToStringArray(packages, ",; \t\n")) {
            for (BeanDefinition candidate : provider.findCandidateComponents(basePackage)) {
                types.add(ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader));
            }
        }
        return types;
    }
}
