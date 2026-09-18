package io.github.serfom256.integration;

import io.github.serfom256.OpenMapperApplication;
import io.github.serfom256.benchmark.DatabaseSetup;
import io.github.serfom256.benchmark.dao.AuthorDao;
import io.github.serfom256.benchmark.dao.ProductDao;
import io.github.serfom256.benchmark.dao.UserDao;
import io.github.serfom256.benchmark.dao.AuthorStatsDao;
import io.github.serfom256.benchmark.dao.AuditedNoteDao;
import io.github.serfom256.benchmark.dao.CatalogItemDao;
import io.github.serfom256.benchmark.dao.JsonDocDao;
import io.github.serfom256.benchmark.dao.TicketDao;
import io.github.serfom256.benchmark.dao.StudentDao;
import io.github.serfom256.benchmark.dao.TaskDao;
import io.github.serfom256.benchmark.dao.InventoryDao;
import io.github.serfom256.benchmark.dao.VersionedItemDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.*;

/**
 * Base for OpenMapper integration tests. Bootstraps the starter through a real
 * Spring Boot context: settings come from {@code application.yaml} (the {@code it}
 * profile), and the Testcontainers DataSource is contributed by
 * {@link IntegrationDataSourceConfig}. No manual context wiring or property maps.
 */
@SpringBootTest(classes = {OpenMapperApplication.class, IntegrationDataSourceConfig.class})
@ActiveProfiles("it")
public abstract class IntegrationTestBase {

    @Autowired
    protected ProductDao productDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected AuthorDao authorDao;

    @Autowired
    protected VersionedItemDao versionedItemDao;

    @Autowired
    protected InventoryDao inventoryDao;

    @Autowired
    protected AuthorStatsDao authorStatsDao;

    @Autowired
    protected TaskDao taskDao;

    @Autowired
    protected StudentDao studentDao;

    @Autowired
    protected AuditedNoteDao auditedNoteDao;

    @Autowired
    protected CatalogItemDao catalogItemDao;

    @Autowired
    protected TicketDao ticketDao;

    @Autowired
    protected JsonDocDao jsonDocDao;

    protected Connection getConnection() throws SQLException {
        return DatabaseSetup.getDataSource().getConnection();
    }

    protected long insertProductViaJdbc(String name, double price, int qty, String desc) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO product (name, price, quantity, description) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, qty);
            ps.setString(4, desc);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected long insertAuthorViaJdbc(String name, String country) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO author (name, country) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, country);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void insertBookViaJdbc(String title, long authorId, int year, String isbn) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO book (title, author_id, published_year, isbn) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, title);
            ps.setLong(2, authorId);
            ps.setInt(3, year);
            ps.setString(4, isbn);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean existsInDb(String table, long id) {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
