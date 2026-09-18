package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.exceptions.internal.ResultSetExtractionException;

class StreamingTest extends IntegrationTestBase {

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String seed(int count) {
        String name = "stream-" + UUID.randomUUID();
        for (int i = 0; i < count; i++) {
            insertProductViaJdbc(name, 1.0, 1, "streamed");
        }
        return name;
    }

    private String shortIsbn() {
        return UUID.randomUUID().toString().substring(0, 18);
    }

    private void cleanUp(String name) {
        productDao.findByName(name).forEach(p -> productDao.deleteProduct(p.getId()));
    }

    @Test
    @DisplayName("a Stream return type yields every row")
    void streamYieldsAllRows() {
        String name = seed(25);
        new TransactionTemplate(transactionManager).execute(status -> {
            try (Stream<ProductOM> products = productDao.streamByName(name)) {
                assertEquals(25, products.count());
            }
            return null;
        });
        cleanUp(name);
    }

    @Test
    @DisplayName("a stream can be closed early without consuming it")
    void streamCanBeAbandonedEarly() {
        String name = seed(25);
        new TransactionTemplate(transactionManager).execute(status -> {
            try (Stream<ProductOM> products = productDao.streamByName(name)) {
                List<ProductOM> firstThree = products.limit(3).toList();
                assertEquals(3, firstThree.size());
            }
            return null;
        });
        cleanUp(name);
    }

    @Test
    @DisplayName("closing the stream releases the connection back to the pool")
    void closingReleasesTheConnection() {
        String name = seed(5);
        new TransactionTemplate(transactionManager).execute(status -> {
            for (int i = 0; i < 40; i++) {
                try (Stream<ProductOM> products = productDao.streamByName(name)) {
                    assertEquals(5, products.count());
                }
            }
            return null;
        });
        cleanUp(name);
    }

    @Test
    @DisplayName("a @Joined entity streams, with each author's books collected")
    void joinedEntityStreams() {
        long authorId = insertAuthorViaJdbc("stream-author-" + UUID.randomUUID(), "SE");
        insertBookViaJdbc("b1", authorId, 2001, shortIsbn());
        insertBookViaJdbc("b2", authorId, 2002, shortIsbn());

        new TransactionTemplate(transactionManager).execute(status -> {
            try (Stream<AuthorOM> authors = authorDao.streamAuthorsWithBooks()) {
                AuthorOM streamed = authors.filter(a -> a.getId() == authorId).findFirst().orElseThrow();
                assertEquals(2, streamed.getBooks().size());
            }
            return null;
        });

        authorDao.deleteAuthor(authorId);
    }

    @Test
    @DisplayName("each author is emitted once, not once per joined book")
    void joinedStreamEmitsEachRootOnce() {
        long first = insertAuthorViaJdbc("once-a-" + UUID.randomUUID(), "SE");
        long second = insertAuthorViaJdbc("once-b-" + UUID.randomUUID(), "NO");
        insertBookViaJdbc("x1", first, 2001, shortIsbn());
        insertBookViaJdbc("x2", first, 2002, shortIsbn());
        insertBookViaJdbc("y1", second, 2003, shortIsbn());

        new TransactionTemplate(transactionManager).execute(status -> {
            try (Stream<AuthorOM> authors = authorDao.streamAuthorsWithBooks()) {
                List<Long> ids = authors.map(AuthorOM::getId)
                        .filter(id -> id == first || id == second)
                        .toList();
                assertEquals(List.of(first, second), ids);
            }
            return null;
        });

        authorDao.deleteAuthor(first);
        authorDao.deleteAuthor(second);
    }

    @Test
    @DisplayName("a query that interleaves roots is rejected rather than yielding split entities")
    void unorderedJoinedStreamIsRejected() {
        long first = insertAuthorViaJdbc("mix-a-" + UUID.randomUUID(), "SE");
        long second = insertAuthorViaJdbc("mix-b-" + UUID.randomUUID(), "NO");
        insertBookViaJdbc("m1", first, 2001, shortIsbn());
        insertBookViaJdbc("m2", second, 2002, shortIsbn());
        insertBookViaJdbc("m3", first, 2003, shortIsbn());

        new TransactionTemplate(transactionManager).execute(status -> {
            assertThrows(ResultSetExtractionException.class, () -> {
                try (Stream<AuthorOM> authors = authorDao.streamAuthorsWithBooksUnordered()) {
                    authors.forEach(a -> { });
                }
            });
            return null;
        });

        authorDao.deleteAuthor(first);
        authorDao.deleteAuthor(second);
    }
}
