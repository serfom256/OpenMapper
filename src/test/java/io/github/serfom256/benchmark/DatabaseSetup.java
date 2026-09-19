package io.github.serfom256.benchmark;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public final class DatabaseSetup {

    private static PostgreSQLContainer<?> postgres;
    private static HikariDataSource dataSource;
    private static volatile boolean initialized = false;

    private DatabaseSetup() {}

    public static synchronized void initialize() {
        if (initialized) return;

        postgres = new PostgreSQLContainer<>("postgres:15-alpine");
        postgres.start();

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        dataSource = new HikariDataSource(config);

        createSchema();
        seedData();
        initialized = true;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static HikariDataSource getHikariDataSource() {
        return dataSource;
    }

    private static void createSchema() {
        try (InputStream is = DatabaseSetup.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is == null) throw new IllegalStateException("schema.sql not found on classpath");
            String sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to create schema", e);
        }
    }

    private static void seedData() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            seedProducts(conn, 1000);
            seedUsersWithProfiles(conn, 500);
            seedAuthorsWithBooks(conn, 100, 5);
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed data", e);
        }
    }

    private static void seedProducts(Connection conn, int count) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO product (name, price, quantity, description) VALUES (?, ?, ?, ?)")) {
            for (int i = 0; i < count; i++) {
                ps.setString(1, "Product-" + i);
                ps.setDouble(2, 10.0 + (i % 100));
                ps.setInt(3, 50 + (i % 200));
                ps.setString(4, "Description for product " + i);
                ps.addBatch();
                if (i % 500 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
    }

    private static void seedUsersWithProfiles(Connection conn, int count) throws SQLException {
        try (PreparedStatement userPs = conn.prepareStatement(
                "INSERT INTO bench_user (username, email) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
             PreparedStatement profilePs = conn.prepareStatement(
                "INSERT INTO user_profile (user_id, bio, avatar_url) VALUES (?, ?, ?)")) {

            for (int i = 0; i < count; i++) {
                userPs.setString(1, "user-" + i);
                userPs.setString(2, "user" + i + "@example.com");
                userPs.executeUpdate();

                try (ResultSet keys = userPs.getGeneratedKeys()) {
                    keys.next();
                    long userId = keys.getLong(1);
                    profilePs.setLong(1, userId);
                    profilePs.setString(2, "Bio of user " + i);
                    profilePs.setString(3, "https://avatars.example.com/" + i + ".png");
                    profilePs.addBatch();
                }
                if (i % 200 == 0) profilePs.executeBatch();
            }
            profilePs.executeBatch();
        }
    }

    private static void seedAuthorsWithBooks(Connection conn, int authorCount, int booksPerAuthor) throws SQLException {
        try (PreparedStatement authorPs = conn.prepareStatement(
                "INSERT INTO author (name, country) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS);
             PreparedStatement bookPs = conn.prepareStatement(
                "INSERT INTO book (title, author_id, published_year, isbn) VALUES (?, ?, ?, ?)")) {

            for (int i = 0; i < authorCount; i++) {
                authorPs.setString(1, "Author-" + i);
                authorPs.setString(2, i % 2 == 0 ? "US" : "UK");
                authorPs.executeUpdate();

                try (ResultSet keys = authorPs.getGeneratedKeys()) {
                    keys.next();
                    long authorId = keys.getLong(1);
                    for (int j = 0; j < booksPerAuthor; j++) {
                        bookPs.setString(1, "Book-" + i + "-" + j);
                        bookPs.setLong(2, authorId);
                        bookPs.setInt(3, 2000 + (j % 24));
                        bookPs.setString(4, "ISBN-" + i + "-" + j);
                        bookPs.addBatch();
                    }
                }
            }
            bookPs.executeBatch();
        }
    }

    public static long[] insertDeleteProducts(int count) {
        long[] ids = new long[count];
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO product (name, price, quantity, description) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            conn.setAutoCommit(false);
            for (int i = 0; i < count; i++) {
                ps.setString(1, "del-" + i);
                ps.setDouble(2, 1.0);
                ps.setInt(3, 1);
                ps.setString(4, "to-delete");
                ps.addBatch();
            }
            ps.executeBatch();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int idx = 0;
                while (rs.next()) {
                    ids[idx++] = rs.getLong(1);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed delete products", e);
        }
        return ids;
    }

    public static long[] insertDeleteAuthorsWithBooks(int authorCount, int booksPerAuthor) {
        long[] authorIds = new long[authorCount];
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement authorPs = conn.prepareStatement(
                    "INSERT INTO author (name, country) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement bookPs = conn.prepareStatement(
                    "INSERT INTO book (title, author_id, published_year, isbn) VALUES (?, ?, ?, ?)")) {

                for (int i = 0; i < authorCount; i++) {
                    authorPs.setString(1, "del-author-" + i);
                    authorPs.setString(2, "XX");
                    authorPs.executeUpdate();
                    try (ResultSet keys = authorPs.getGeneratedKeys()) {
                        keys.next();
                        authorIds[i] = keys.getLong(1);
                    }
                    for (int j = 0; j < booksPerAuthor; j++) {
                        bookPs.setString(1, "del-book-" + i + "-" + j);
                        bookPs.setLong(2, authorIds[i]);
                        bookPs.setInt(3, 2020);
                        bookPs.setString(4, "DEL-" + i + "-" + j);
                        bookPs.addBatch();
                    }
                }
                bookPs.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed delete authors", e);
        }
        return authorIds;
    }
}
