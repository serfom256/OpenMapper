package io.github.serfom256.unit.core.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.annotations.entity.Field;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.annotations.entity.OptimisticLockField;
import io.github.serfom256.core.common.ModelSpecificationReader;
import io.github.serfom256.core.query.mapping.MappingPlanRegistry;
import io.github.serfom256.exceptions.entity.ModelValidationException;

/**
 * Model problems are reported when the model is read, not on the first query that
 * happens to touch it.
 */
class ModelValidationTest {

    private final ModelSpecificationReader reader = new ModelSpecificationReader();
    private final MappingPlanRegistry plans = new MappingPlanRegistry(null);

    @Model(primaryKey = "id")
    static class BadLockType {
        @Field(name = "id") long id;
        @OptimisticLockField(oldVersion = "old_version") String version;
    }

    @Model(primaryKey = "id")
    static class GoodLockType {
        @Field(name = "id") long id;
        @OptimisticLockField(oldVersion = "old_version") long version;
    }

    @Model(primaryKey = "id")
    static class TwoLockFields {
        @Field(name = "id") long id;
        @OptimisticLockField(oldVersion = "a") long first;
        @OptimisticLockField(oldVersion = "b") long second;
    }

    @Model(primaryKey = "missing")
    static class UnmappedPrimaryKey {
        @Field(name = "id") long id;
    }

    @Test
    @DisplayName("an optimistic lock field of an unsupported type names the type and the alternatives")
    void unsupportedLockType() {
        assertThatThrownBy(() -> reader.readModelMetadata(BadLockType.class))
                .isInstanceOf(ModelValidationException.class)
                .hasMessageContaining("version")
                .hasMessageContaining("String");
    }

    @Test
    @DisplayName("supported lock types pass")
    void supportedLockType() {
        assertThatCode(() -> reader.readModelMetadata(GoodLockType.class)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("more than one optimistic lock field is rejected")
    void multipleLockFields() {
        assertThatThrownBy(() -> reader.readModelMetadata(TwoLockFields.class))
                .isInstanceOf(ModelValidationException.class)
                .hasMessageContaining("expected 1");
    }

    @Test
    @DisplayName("a primary key with no @Field behind it is rejected when the plan is compiled")
    void unmappedPrimaryKey() {
        assertThatThrownBy(() -> plans.planFor(UnmappedPrimaryKey.class))
                .isInstanceOf(ModelValidationException.class)
                .hasMessageContaining("primary key");
    }

    @Test
    @DisplayName("compiling a plan twice returns the same instance")
    void plansAreCached() {
        assertThat(plans.planFor(GoodLockType.class)).isSameAs(plans.planFor(GoodLockType.class));
    }
}
