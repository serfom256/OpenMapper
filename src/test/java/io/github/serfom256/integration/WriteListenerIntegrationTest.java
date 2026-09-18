package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.BookOM;
import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.core.lifecycle.EntityWriteListener;
import io.github.serfom256.model.operations.DmlOperation;

@Import(WriteListenerIntegrationTest.ListenerConfig.class)
class WriteListenerIntegrationTest extends IntegrationTestBase {

    static final List<String> SEEN = new CopyOnWriteArrayList<>();

    @TestConfiguration
    static class ListenerConfig {
        @Bean
        EntityWriteListener<Object> recorder() {
            return new EntityWriteListener<>() {
                public Class<Object> type() {
                    return Object.class;
                }

                public void beforeWrite(Object entity, DmlOperation operation) {
                    SEEN.add(entity.getClass().getSimpleName() + ":" + operation);
                    if (entity instanceof ProductOM product && product.getDescription() == null) {
                        product.setDescription("filled by listener");
                    }
                }
            };
        }
    }

    @BeforeEach
    void clear() {
        SEEN.clear();
    }

    @Test
    @DisplayName("a listener fires on a single write and its changes are persisted")
    void singleWrite() {
        String name = "listener-" + UUID.randomUUID();
        ProductOM product = new ProductOM();
        product.setName(name);
        product.setPrice(1.0);
        product.setQuantity(1);

        productDao.insertProduct(product);

        assertTrue(SEEN.contains("ProductOM:INSERT"));
        assertEquals("filled by listener", productDao.findByName(name).get(0).getDescription());
        productDao.findByName(name).forEach(p -> productDao.deleteProduct(p.getId()));
    }

    @Test
    @DisplayName("a listener fires for every element of a batch")
    void batchWrite() {
        String name = "listener-batch-" + UUID.randomUUID();
        List<ProductOM> products = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductOM product = new ProductOM();
            product.setName(name);
            product.setPrice(1.0);
            product.setQuantity(1);
            products.add(product);
        }

        productDao.insertProducts(products);

        assertEquals(5, SEEN.stream().filter(s -> s.equals("ProductOM:INSERT")).count());
        productDao.findByName(name).forEach(p -> productDao.deleteProduct(p.getId()));
    }

    @Test
    @DisplayName("a listener fires for cascaded children as well as the parent")
    void cascadeWrite() {
        AuthorOM author = new AuthorOM();
        author.setName("listener-cascade-" + UUID.randomUUID());
        author.setCountry("SE");
        List<BookOM> books = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            BookOM book = new BookOM();
            book.setTitle("b" + i);
            book.setPublishedYear(2020);
            book.setIsbn(UUID.randomUUID().toString().substring(0, 18));
            books.add(book);
        }
        author.setBooks(books);

        long authorId = authorDao.insertAuthorWithBooks(author);

        assertTrue(SEEN.contains("AuthorOM:INSERT"));
        assertEquals(2, SEEN.stream().filter(s -> s.equals("BookOM:INSERT")).count());
        authorDao.deleteAuthor(authorId);
    }
}
