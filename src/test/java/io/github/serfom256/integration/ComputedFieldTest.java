package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.AuthorStatsOM;

class ComputedFieldTest extends IntegrationTestBase {

    @Test
    @DisplayName("a read-only field is populated from a computed column")
    void readOnlyFieldIsMapped() {
        long authorId = insertAuthorViaJdbc("computed-" + UUID.randomUUID(), "SE");
        insertBookViaJdbc("c1", authorId, 2001, UUID.randomUUID().toString().substring(0, 18));
        insertBookViaJdbc("c2", authorId, 2002, UUID.randomUUID().toString().substring(0, 18));

        AuthorStatsOM stats = authorStatsDao.findWithBookCount(authorId);

        assertEquals(2, stats.getBookCount());
        authorDao.deleteAuthor(authorId);
    }

    @Test
    @DisplayName("the same model writes without the computed column, which has no place in an insert")
    void readOnlyFieldIsNotWritten() {
        AuthorStatsOM author = new AuthorStatsOM();
        author.setName("computed-write-" + UUID.randomUUID());
        author.setCountry("NO");
        author.setBookCount(99);

        long id = authorStatsDao.insert(author);

        assertEquals(0, authorStatsDao.findWithBookCount(id).getBookCount());
        authorDao.deleteAuthor(id);
    }
}
