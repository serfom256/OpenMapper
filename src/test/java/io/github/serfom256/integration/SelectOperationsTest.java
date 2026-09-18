package io.github.serfom256.integration;

import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.BookOM;
import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.benchmark.model.om.UserOM;
import io.github.serfom256.benchmark.model.om.UserProfileOM;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SelectOperationsTest extends IntegrationTestBase {

    @Test
    void selectProductById_returnsCorrectFields() {
        ProductOM product = productDao.findProductById(1L);
        assertNotNull(product);
        assertEquals("Product-0", product.getName());
        assertTrue(product.getPrice() >= 10.0);
        assertTrue(product.getQuantity() >= 50);
        assertNotNull(product.getDescription());
    }

    @Test
    void selectProductById_differentIds() {
        ProductOM p1 = productDao.findProductById(1L);
        ProductOM p2 = productDao.findProductById(2L);
        assertNotNull(p1);
        assertNotNull(p2);
        assertNotEquals(p1.getName(), p2.getName());
    }

    @Test
    void selectProductById_nonExistentId_returnsNull() {
        ProductOM product = productDao.findProductById(999_999L);
        assertNull(product);
    }

    @Test
    void selectBatchProducts_returnsList() {
        List<ProductOM> products = productDao.findProducts(10);
        assertNotNull(products);
        assertEquals(10, products.size());
        for (ProductOM p : products) {
            assertNotNull(p.getName());
            assertTrue(p.getPrice() > 0);
        }
    }

    @Test
    void selectBatchProducts_respectsLimit() {
        List<ProductOM> small = productDao.findProducts(5);
        List<ProductOM> large = productDao.findProducts(50);
        assertEquals(5, small.size());
        assertEquals(50, large.size());
    }

    @Test
    void selectUserWithProfile_returnsJoinedEntities() {
        UserOM user = userDao.findUserWithProfile(1L);
        assertNotNull(user);
        assertNotNull(user.getUsername());
        assertTrue(user.getUsername().startsWith("user-"));
        assertNotNull(user.getEmail());

        UserProfileOM profile = user.getProfile();
        assertNotNull(profile);
        assertNotNull(profile.getBio());
        assertNotNull(profile.getAvatarUrl());
        assertEquals(user.getId(), profile.getUserId());
    }

    @Test
    void selectUserWithProfile_nonExistent_returnsNull() {
        UserOM user = userDao.findUserWithProfile(999_999L);
        assertNull(user);
    }

    @Test
    void selectAuthorWithBooks_returnsJoinedCollection() {
        AuthorOM author = authorDao.findAuthorWithBooks(1L);
        assertNotNull(author);
        assertNotNull(author.getName());
        assertTrue(author.getName().startsWith("Author-"));

        List<BookOM> books = author.getBooks();
        assertNotNull(books);
        assertEquals(5, books.size());
        for (BookOM book : books) {
            assertNotNull(book.getTitle());
            assertEquals(author.getId(), book.getAuthorId());
            assertTrue(book.getPublishedYear() >= 2000);
        }
    }

    @Test
    void selectAuthorWithBooks_nonExistent_returnsNull() {
        AuthorOM author = authorDao.findAuthorWithBooks(999_999L);
        assertNull(author);
    }

    @Test
    void selectMultipleAuthors_eachHasCorrectBooks() {
        AuthorOM a1 = authorDao.findAuthorWithBooks(1L);
        AuthorOM a2 = authorDao.findAuthorWithBooks(2L);
        assertNotNull(a1);
        assertNotNull(a2);
        assertNotEquals(a1.getName(), a2.getName());
        assertEquals(5, a1.getBooks().size());
        assertEquals(5, a2.getBooks().size());
    }
}
