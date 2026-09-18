package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.BookOM;

class CascadeWriteTest extends IntegrationTestBase {

    private BookOM book(String title) {
        BookOM book = new BookOM();
        book.setTitle(title);
        book.setPublishedYear(2020);
        book.setIsbn(UUID.randomUUID().toString().substring(0, 18));
        return book;
    }

    private AuthorOM author(String name, int bookCount) {
        AuthorOM author = new AuthorOM();
        author.setName(name);
        author.setCountry("SE");
        List<BookOM> books = new ArrayList<>();
        for (int i = 0; i < bookCount; i++) {
            books.add(book("cascaded-" + i));
        }
        author.setBooks(books);
        return author;
    }

    @Test
    @DisplayName("saving a parent writes its @Joined children under the generated key")
    void cascadeInsertsChildren() {
        AuthorOM author = author("cascade-" + UUID.randomUUID(), 3);

        long authorId = authorDao.insertAuthorWithBooks(author);

        AuthorOM reloaded = authorDao.findAuthorWithBooks(authorId);
        assertEquals(3, reloaded.getBooks().size());
        assertTrue(reloaded.getBooks().stream().allMatch(b -> b.getAuthorId() == authorId));

        authorDao.deleteAuthor(authorId);
    }

    @Test
    @DisplayName("a parent with an empty collection writes only itself")
    void emptyCollectionWritesNothingExtra() {
        AuthorOM author = author("cascade-empty-" + UUID.randomUUID(), 0);

        long authorId = authorDao.insertAuthorWithBooks(author);

        assertEquals(0, authorDao.findAuthorWithBooks(authorId).getBooks().size());

        authorDao.deleteAuthor(authorId);
    }

    @Test
    @DisplayName("children are written in one batch, so many of them stay cheap")
    void manyChildrenAreBatched() {
        AuthorOM author = author("cascade-many-" + UUID.randomUUID(), 100);

        long authorId = authorDao.insertAuthorWithBooks(author);

        assertEquals(100, authorDao.findAuthorWithBooks(authorId).getBooks().size());

        authorDao.deleteAuthor(authorId);
    }
}
