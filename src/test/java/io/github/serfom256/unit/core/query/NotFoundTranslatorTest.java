package io.github.serfom256.unit.core.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.serfom256.annotations.DaoMethod;
import io.github.serfom256.core.query.common.NotFoundTranslator;
import io.github.serfom256.exceptions.entity.EntityNotFoundException;
import io.github.serfom256.unit.support.TestPlans;

class NotFoundTranslatorTest {

    // These are public with public constructors on purpose: openMapper instantiates
    // the configured type reflectively, so it must be publicly constructible.

    public static class NoArgOnly extends RuntimeException {
        public NoArgOnly() {
        }
    }

    public static class Unconstructible extends RuntimeException {
        public Unconstructible(int unusable) {
            super(String.valueOf(unusable));
        }
    }

    public static class CheckedFailure extends Exception {
        public CheckedFailure(String message) {
            super(message);
        }
    }

    interface Dao {
        @DaoMethod
        String untranslated();

        @DaoMethod(translateNotFoundExceptionTo = EntityNotFoundException.class)
        String translated();

        @DaoMethod(translateNotFoundExceptionTo = EntityNotFoundException.class)
        List<String> translatedCollection();

        @DaoMethod(translateNotFoundExceptionTo = EntityNotFoundException.class)
        Optional<String> translatedOptional();

        @DaoMethod(translateNotFoundExceptionTo = CheckedFailure.class)
        String checkedUndeclared();

        @DaoMethod(translateNotFoundExceptionTo = CheckedFailure.class)
        String checkedDeclared() throws CheckedFailure;

        String unannotated();
    }

    private final NotFoundTranslator translator = new NotFoundTranslator();

    private static Method method(String name) throws NoSuchMethodException {
        return Dao.class.getMethod(name);
    }

    @Nested
    @DisplayName("what the plan decides")
    class PlanResolution {

        @Test
        @DisplayName("translation is opt-in: the default resolves to no exception")
        void defaultIsNoTranslation() {
            assertThat(TestPlans.planFor(Dao.class, "untranslated").notFoundException()).isNull();
        }

        @Test
        @DisplayName("an unannotated method resolves to no exception")
        void unannotatedIsNoTranslation() {
            assertThat(TestPlans.planFor(Dao.class, "unannotated").notFoundException()).isNull();
        }

        @Test
        @DisplayName("a configured exception is resolved once, at plan time")
        void configuredExceptionIsResolved() {
            assertThat(TestPlans.planFor(Dao.class, "translated").notFoundException())
                    .isEqualTo(EntityNotFoundException.class);
        }

        @Test
        @DisplayName("return types that express absence themselves are never translated")
        void absenceTolerantReturnTypesResolveToNull() {
            assertThat(TestPlans.planFor(Dao.class, "translatedCollection").notFoundException()).isNull();
            assertThat(TestPlans.planFor(Dao.class, "translatedOptional").notFoundException()).isNull();
        }
    }

    @Nested
    @DisplayName("what the translator does with that decision")
    class Translation {

        @Test
        @DisplayName("a present result is never translated")
        void presentResultPassesThrough() throws Exception {
            assertThatCode(() -> translator.translateIfAbsent(
                    EntityNotFoundException.class, method("translated"), "value"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("no configured exception means the null result stands")
        void noTargetMeansNoThrow() throws Exception {
            assertThatCode(() -> translator.translateIfAbsent(null, method("untranslated"), null))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("an absent result throws, naming the method")
        void absentResultThrows() throws Exception {
            assertThatThrownBy(() -> translator.translateIfAbsent(
                    EntityNotFoundException.class, method("translated"), null))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Dao.translated()");
        }

        @Test
        @DisplayName("an exception with only a no-arg constructor is still usable")
        void noArgConstructorFallback() throws Exception {
            assertThatThrownBy(() -> translator.translateIfAbsent(
                    NoArgOnly.class, method("translated"), null))
                    .isInstanceOf(NoArgOnly.class);
        }

        @Test
        @DisplayName("an exception openMapper cannot construct reports why")
        void unconstructibleExceptionIsReported() throws Exception {
            assertThatThrownBy(() -> translator.translateIfAbsent(
                    Unconstructible.class, method("translated"), null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("(String) or no-argument constructor");
        }

        @Test
        @DisplayName("a checked exception the method does not declare is rejected with guidance")
        void undeclaredCheckedExceptionIsRejected() throws Exception {
            assertThatThrownBy(() -> translator.translateIfAbsent(
                    CheckedFailure.class, method("checkedUndeclared"), null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("must declare it in a throws clause");
        }

        @Test
        @DisplayName("a checked exception the method declares propagates as itself")
        void declaredCheckedExceptionPropagates() throws Exception {
            assertThatThrownBy(() -> translator.translateIfAbsent(
                    CheckedFailure.class, method("checkedDeclared"), null))
                    .isInstanceOf(CheckedFailure.class);
        }
    }
}
