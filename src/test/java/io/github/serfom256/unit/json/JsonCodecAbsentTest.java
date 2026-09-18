package io.github.serfom256.unit.json;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Json;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.exceptions.entity.ModelValidationException;

/**
 * Jackson is an optional dependency, so a {@code @Json} field can be declared with no
 * codec on the classpath. That has to fail at startup with an actionable message rather
 * than at the first query with a null pointer.
 */
class JsonCodecAbsentTest {

    @Model(primaryKey = "id")
    public static class WithJson {

        @Field(name = "id")
        long id;

        @Field(name = "attributes")
        @Json
        Map<String, String> attributes;
    }

    @Model(primaryKey = "id")
    public static class WithoutJson {

        @Field(name = "id")
        long id;

        @Field(name = "name")
        String name;
    }

    @Test
    @DisplayName("a @Json field with no codec fails at plan build, naming the field and the fix")
    void jsonFieldWithoutCodecFailsClearly() {
        MappingPlanRegistry registry = new MappingPlanRegistry(null);

        ModelValidationException failure = assertThrows(ModelValidationException.class,
                () -> registry.planFor(WithJson.class));

        assertTrue(failure.getMessage().contains("attributes"), failure.getMessage());
        assertTrue(failure.getMessage().contains("@Json"), failure.getMessage());
        assertTrue(failure.getMessage().contains("Jackson"), failure.getMessage());
    }

    @Test
    @DisplayName("a model with no @Json field is unaffected by the missing codec")
    void modelWithoutJsonStillBuilds() {
        assertNotNull(new MappingPlanRegistry(null).planFor(WithoutJson.class));
    }
}
