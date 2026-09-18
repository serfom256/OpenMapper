package io.github.serfom256.core.environment.processors;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.entity.Sql;
import io.github.serfom256.config.OpenMapperGlobalEnvironmentVariables;
import io.github.serfom256.core.context.DaoMethodContext;
import io.github.serfom256.core.environment.EnvironmentProcessor;
import io.github.serfom256.core.environment.PackageScanner;

public class DaoMethodEnvironmentProcessor implements EnvironmentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DaoMethodEnvironmentProcessor.class);

    private final PackageScanner scanner;
    private final OpenMapperGlobalEnvironmentVariables variables;
    private final DaoMethodContext daoMethodContext;

    public DaoMethodEnvironmentProcessor(
            PackageScanner scanner,
            OpenMapperGlobalEnvironmentVariables variables,
            DaoMethodContext daoMethodContext) {
        this.scanner = scanner;
        this.variables = variables;
        this.daoMethodContext = daoMethodContext;
    }

    @Override
    public void processEnvironment() {
        Set<Class<?>> classes = scanner.scanPackagesFor(variables.getDaoPackageToScan(), DaoLayer.class);

        for (Class<?> daoLayer : classes) {
            if (variables.isLoggingEnabled()) {
                logger.info("Found repository: {}", daoLayer.getName());
            }
            for (Method method : daoLayer.getDeclaredMethods()) {
                if (containsSqlProcedureParameter(method)) {
                    daoMethodContext.registerMethod(method);
                }
            }
        }
    }

    private boolean containsSqlProcedureParameter(Method method) {
        Parameter[] params = method.getParameters();
        for (Parameter param : params) {
            if (param.getAnnotation(Sql.class) != null) {
                return true;
            }
        }
        return false;
    }
}
