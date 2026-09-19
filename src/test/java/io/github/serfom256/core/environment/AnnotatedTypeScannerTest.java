package io.github.serfom256.core.environment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.annotations.DaoLayer;
import io.github.serfom256.annotations.entity.Model;
import io.github.serfom256.benchmark.dao.ProductDao;
import io.github.serfom256.benchmark.model.om.AuditedBase;
import io.github.serfom256.benchmark.model.om.BundleItemOM;
import io.github.serfom256.benchmark.model.om.CatalogItemOM;
import io.github.serfom256.benchmark.model.om.JsonSpecOM;
import io.github.serfom256.benchmark.model.om.ProductOM;

class AnnotatedTypeScannerTest {
    private final AnnotatedTypeScanner scanner = new AnnotatedTypeScanner(null, null);

    @Test
    @DisplayName("finds interfaces, which Spring's component scan skips by default")
    void findsDaoInterfaces() {
        assertTrue(scanner.scan("io.github.serfom256.benchmark", DaoLayer.class).contains(ProductDao.class));
    }

    @Test
    @DisplayName("finds subclasses of a @Model type, because @Model is @Inherited")
    void findsInheritedModelSubtypes() {
        Set<Class<?>> models = scanner.scan("io.github.serfom256.benchmark.model.om", Model.class);

        assertTrue(models.contains(CatalogItemOM.class));
        assertTrue(models.contains(BundleItemOM.class), "polymorphic subtypes must be registered");
    }

    @Test
    @DisplayName("leaves out classes that carry no annotation, abstract or not")
    void skipsUnannotatedTypes() {
        Set<Class<?>> models = scanner.scan("io.github.serfom256.benchmark.model.om", Model.class);

        assertFalse(models.contains(JsonSpecOM.class));
        assertFalse(models.contains(AuditedBase.class));
    }

    @Test
    @DisplayName("accepts several comma-separated packages")
    void scansSeveralPackages() {
        Set<Class<?>> types = scanner.scan(
                "io.github.serfom256.benchmark.dao, io.github.serfom256.benchmark.model.om", Model.class);

        assertTrue(types.contains(ProductOM.class));
    }

    @Test
    @DisplayName("stays inside the packages it was given")
    void staysInsideGivenPackages() {
        assertTrue(scanner.scan("io.github.serfom256.benchmark.dao", Model.class).isEmpty());
    }
}
