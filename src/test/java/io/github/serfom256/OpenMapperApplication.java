package io.github.serfom256;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test-only bootstrap application. openMapper ships no {@code @SpringBootApplication}
 * of its own: the starter is wired purely through auto-configuration, so this class
 * exists only to give the integration tests a context to load.
 */
@SpringBootApplication
public class OpenMapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenMapperApplication.class, args);
    }
}
