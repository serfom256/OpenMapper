package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import io.github.serfom256.benchmark.model.om.ProductOM;

/**
 * A chunk size this small is not realistic; it makes the generated SQL take the
 * chunked shape for a handful of rows so a real database has to parse and run it.
 */
@TestPropertySource(properties = "openmapper.in.clause.chunk.size=2")
class InClauseChunkingTest extends IntegrationTestBase {

    private final List<Long> created = new ArrayList<>();

    private List<Long> insert(String description, int count) {
        for (int i = 0; i < count; i++) {
            created.add(insertProductViaJdbc("chunk-%d-%s".formatted(i, UUID.randomUUID()),
                    1.0, 1, description));
        }
        return List.copyOf(created);
    }

    @Test
    @DisplayName("an IN list longer than the chunk size still selects exactly its elements")
    void chunkedInListRuns() {
        String description = "chunk-in-" + UUID.randomUUID();
        List<Long> ids = insert(description, 5);

        List<ProductOM> found = productDao.findProductsByIds(ids);

        assertEquals(5, found.size());
        assertEquals(ids, found.stream().map(ProductOM::getId).toList());
    }

    @Test
    @DisplayName("a chunked NOT IN list excludes exactly its elements")
    void chunkedNotInListRuns() {
        String description = "chunk-not-in-" + UUID.randomUUID();
        List<Long> ids = insert(description, 5);
        List<Long> excluded = ids.subList(0, 3);

        List<ProductOM> found = productDao.findProductsExcludingIds(description, excluded);

        assertEquals(2, found.size());
        assertTrue(found.stream().noneMatch(p -> excluded.contains(p.getId())));
    }

    @Test
    @DisplayName("an empty exclusion list keeps every row")
    void emptyNotInKeepsEveryRow() {
        String description = "chunk-empty-" + UUID.randomUUID();
        insert(description, 3);

        List<ProductOM> found = productDao.findProductsExcludingIds(description, List.of());

        assertEquals(3, found.size());
    }

    @Test
    @DisplayName("an empty IN list selects nothing")
    void emptyInSelectsNothing() {
        insert("chunk-none-" + UUID.randomUUID(), 2);

        assertTrue(productDao.findProductsByIds(List.of()).isEmpty());
    }
}
