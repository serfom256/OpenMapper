package io.github.serfom256.annotations;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a class a Spring component that consumes openMapper DAOs.
 *
 * <p>{@code @DaoLayer} proxies are registered during startup rather than found
 * by component scanning, so a bean that injects one must not be created before
 * that has happened. This annotation is meta-annotated
 * {@link Component @Component} and {@link DependsOn @DependsOn} against
 * openMapper's environment processor, which orders construction correctly.
 *
 * <p>Use it in place of {@code @Service} or {@code @Component} on classes that
 * inject a DAO:
 *
 * <pre>{@code
 * @UseRepository
 * public class UserService {
 *     private final UserDao userDao;
 *     public UserService(UserDao userDao) { this.userDao = userDao; }
 * }
 * }</pre>
 *
 * @see DaoLayer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
@DependsOn(value = "openMapperEnvironmentProcessor")
public @interface UseRepository {
}
