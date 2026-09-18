package io.github.serfom256.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a data access layer. openMapper discovers these at
 * startup, generates a proxy implementation for each, and registers it as a bean
 * under the interface's simple name, so it can be injected like any other bean.
 *
 * <p>Only the packages named by {@code openmapper.dao.packagesToScan} are
 * searched. When that property is unset, scanning falls back to the package of
 * the application's {@code @SpringBootApplication} class.
 *
 * <pre>{@code
 * @DaoLayer
 * public interface UserDao {
 *     @DaoMethod(procedure = "findUserById")
 *     User findById(@Param(name = "id") long id);
 * }
 * }</pre>
 *
 * @see DaoMethod
 * @see UseRepository
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DaoLayer {

    /**
     * Name of the {@link javax.sql.DataSource} bean this DAO executes against.
     * Use it to bind a DAO to a specific database in a multi-datasource
     * application; the default matches Spring Boot's single-datasource setup.
     *
     * @return the DataSource bean name, {@code "dataSource"} by default
     */
    String dataSource() default "dataSource";
}
