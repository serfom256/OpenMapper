package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.model.page.Page;
import io.github.serfom256.model.page.PageRequest;

class PaginationTest extends IntegrationTestBase {

    private String seed(int count) {
        String name = "page-" + UUID.randomUUID();
        for (int i = 0; i < count; i++) {
            insertProductViaJdbc(name, i, i, "paged");
        }
        return name;
    }

    private void cleanUp(String name) {
        productDao.findByName(name).forEach(p -> productDao.deleteProduct(p.getId()));
    }

    @Test
    @DisplayName("a page carries its slice and the total across all pages")
    void firstPage() {
        String name = seed(25);

        Page<ProductOM> first = productDao.findPagedByName(name, PageRequest.of(0, 10));

        assertEquals(10, first.content().size());
        assertEquals(25, first.totalElements());
        assertEquals(3, first.totalPages());
        assertEquals(0, first.page());
        assertTrue(first.hasNext());

        cleanUp(name);
    }

    @Test
    @DisplayName("the offset advances with the page index and the last page is short")
    void lastPage() {
        String name = seed(25);

        Page<ProductOM> last = productDao.findPagedByName(name, PageRequest.of(2, 10));

        assertEquals(5, last.content().size());
        assertEquals(25, last.totalElements());
        assertFalse(last.hasNext());

        cleanUp(name);
    }

    @Test
    @DisplayName("pages do not overlap")
    void pagesAreDisjoint() {
        String name = seed(25);

        Page<ProductOM> first = productDao.findPagedByName(name, PageRequest.of(0, 10));
        Page<ProductOM> second = productDao.findPagedByName(name, PageRequest.of(1, 10));

        var firstIds = first.content().stream().map(ProductOM::getId).toList();
        var secondIds = second.content().stream().map(ProductOM::getId).toList();
        assertTrue(firstIds.stream().noneMatch(secondIds::contains));

        cleanUp(name);
    }

    @Test
    @DisplayName("a page past the end is empty but still reports the total")
    void pagePastTheEnd() {
        String name = seed(5);

        Page<ProductOM> beyond = productDao.findPagedByName(name, PageRequest.of(10, 10));

        assertTrue(beyond.isEmpty());
        assertEquals(5, beyond.totalElements());
        assertFalse(beyond.hasNext());

        cleanUp(name);
    }
}
