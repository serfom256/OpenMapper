package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import io.github.serfom256.benchmark.model.om.AuthorOM;
import io.github.serfom256.benchmark.model.om.ProductOM;
import io.github.serfom256.model.operations.DmlOperation;
import io.github.serfom256.procedures.Procedures;

class ProceduresTest extends IntegrationTestBase {

    @Autowired
    protected Procedures procedures;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private String uniqueName() {
        return "proc-" + UUID.randomUUID();
    }

    @Test
    @DisplayName("an inline select maps a @Model exactly as a DAO method would")
    void selectMapsModel() {
        String name = uniqueName();
        long id = insertProductViaJdbc(name, 9.5, 3, "inline");

        List<ProductOM> found = procedures.select(
                "SELECT id, name, price, quantity, description FROM product WHERE name = [name]",
                ProductOM.class, Map.of("name", name));

        assertEquals(1, found.size());
        assertEquals(id, found.get(0).getId());
        assertEquals(name, found.get(0).getName());
        assertEquals(3, found.get(0).getQuantity());
    }

    @Test
    @DisplayName("an inline select builds the whole object graph, joins included")
    void selectBuildsGraph() {
        AuthorOM author = authorDao.findAuthorWithBooks(1L);
        assertNotNull(author, "fixture author must exist");

        AuthorOM viaProcedures = procedures.selectOne(
                "SELECT a.id, a.name, b.id AS book_id, b.author_id, b.title, b.isbn "
                        + "FROM author a LEFT JOIN book b ON b.author_id = a.id "
                        + "WHERE a.id = [id] ORDER BY a.id",
                AuthorOM.class, Map.of("id", 1L)).orElseThrow();

        assertNotNull(viaProcedures);
        assertEquals(author.getBooks().size(), viaProcedures.getBooks().size());
        assertTrue(viaProcedures.getBooks().size() > 1, "author should have several books");
    }

    @Test
    @DisplayName("a scalar select reads a single value")
    void selectScalar() {
        String name = uniqueName();
        insertProductViaJdbc(name, 1.0, 7, "inline");

        assertEquals(Optional.of(7), procedures.selectOne(
                "SELECT quantity FROM product WHERE name = [name]", Integer.class, Map.of("name", name)));
    }

    @Test
    @DisplayName("selectOne returns null when nothing matched")
    void selectOneMissing() {
        assertTrue(procedures.selectOne("SELECT id FROM product WHERE name = [name]",
                Long.class, Map.of("name", uniqueName())).isEmpty());
    }

    @Test
    @DisplayName("a collection parameter still expands into an IN clause")
    void selectWithInClause() {
        String name = uniqueName();
        long first = insertProductViaJdbc(name + "-a", 1.0, 1, "inline");
        long second = insertProductViaJdbc(name + "-b", 1.0, 1, "inline");

        List<ProductOM> found = procedures.select(
                "SELECT id, name, price, quantity, description FROM product WHERE id IN ([ids]) ORDER BY id",
                ProductOM.class, Map.of("ids", List.of(first, second)));

        assertEquals(List.of(first, second), found.stream().map(ProductOM::getId).toList());
    }

    @Test
    @DisplayName("insert, update and delete all run and report affected rows")
    void writesRunAndReportRows() {
        String name = uniqueName();

        long inserted = procedures.execute(
                "INSERT INTO product (name, price, quantity, description) "
                        + "VALUES ([name], [price], [quantity], [description])",
                DmlOperation.INSERT,
                Map.of("name", name, "price", 2.5, "quantity", 1, "description", "inline"));
        assertEquals(1, inserted);

        long updated = procedures.execute("UPDATE product SET quantity = [quantity] WHERE name = [name]",
                DmlOperation.UPDATE, Map.of("quantity", 42, "name", name));
        assertEquals(1, updated);

        assertEquals(Optional.of(42), procedures.selectOne("SELECT quantity FROM product WHERE name = [name]",
                Integer.class, Map.of("name", name)));

        assertEquals(1, procedures.execute("DELETE FROM product WHERE name = [name]",
                DmlOperation.DELETE, Map.of("name", name)));
    }

    @Test
    @DisplayName("an insert can return the generated key")
    void insertReturnsGeneratedKey() {
        String name = uniqueName();

        Long id = procedures.<Long>executeReturningKey(
                "INSERT INTO product (name, price, quantity, description) "
                        + "VALUES ([name], [price], [quantity], [description])",
                DmlOperation.INSERT, Long.class,
                Map.of("name", name, "price", 1.0, "quantity", 1, "description", "inline")).orElseThrow();

        assertNotNull(id);
        assertEquals(Optional.of(name), procedures.selectOne("SELECT name FROM product WHERE id = [id]",
                String.class, Map.of("id", id)));
    }

    @Test
    @DisplayName("values are bound, never concatenated into the statement")
    void parametersAreBound() {
        String name = uniqueName();
        insertProductViaJdbc(name, 1.0, 1, "inline");

        assertTrue(procedures.select("SELECT id FROM product WHERE name = [name]",
                Long.class, Map.of("name", "' OR '1'='1")).isEmpty());
        assertEquals(1, procedures.select("SELECT id FROM product WHERE name = [name]",
                Long.class, Map.of("name", name)).size());
    }

    @Test
    @DisplayName("a missing parameter fails instead of running a broken statement")
    void missingParameterFails() {
        assertThrows(IllegalArgumentException.class, () -> procedures.select(
                "SELECT id FROM product WHERE name = [name]", Long.class, Map.of("wrong", "x")));
    }

    @Test
    @DisplayName("an inline write joins the ambient transaction and rolls back with it")
    void writeJoinsAmbientTransaction() {
        String name = uniqueName();

        new TransactionTemplate(transactionManager).execute(status -> {
            procedures.execute(
                    "INSERT INTO product (name, price, quantity, description) "
                            + "VALUES ([name], [price], [quantity], [description])",
                    DmlOperation.INSERT,
                    Map.of("name", name, "price", 1.0, "quantity", 1, "description", "inline-tx"));

            assertEquals(1, procedures.select("SELECT id FROM product WHERE name = [name]",
                    Long.class, Map.of("name", name)).size());

            status.setRollbackOnly();
            return null;
        });

        assertTrue(procedures.select("SELECT id FROM product WHERE name = [name]",
                Long.class, Map.of("name", name)).isEmpty(),
                "the inline write survived a rollback, so it ran outside the transaction");
    }
}
