package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.TaskOM;

class EnumMappingTest extends IntegrationTestBase {

    private long insert(String title, TaskOM.Status status, TaskOM.Priority priority) {
        TaskOM task = new TaskOM();
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(priority);
        taskDao.insertTask(task);
        return idOf(title);
    }

    private long idOf(String title) {
        try (var connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT id FROM task WHERE title = ?")) {
            statement.setString(1, title);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int storedStatus(long id) {
        try (var connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT status FROM task WHERE id = ?")) {
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("an ORDINAL enum is written as its position and read back as the constant")
    void ordinalRoundTrip() {
        long id = insert("enum-" + UUID.randomUUID(), TaskOM.Status.IN_PROGRESS, TaskOM.Priority.HIGH);

        assertEquals(1, storedStatus(id));
        assertEquals(TaskOM.Status.IN_PROGRESS, taskDao.findTaskById(id).getStatus());
    }

    @Test
    @DisplayName("a NAME enum still round-trips by name in the same model")
    void nameRoundTripAlongsideOrdinal() {
        long id = insert("enum-name-" + UUID.randomUUID(), TaskOM.Status.DONE, TaskOM.Priority.LOW);

        TaskOM loaded = taskDao.findTaskById(id);
        assertEquals(TaskOM.Priority.LOW, loaded.getPriority());
        assertEquals(TaskOM.Status.DONE, loaded.getStatus());
        assertEquals(2, storedStatus(id));
    }

    @Test
    @DisplayName("ordinals are queryable as the numbers actually stored")
    void ordinalIsQueryable() {
        String title = "enum-query-" + UUID.randomUUID();
        long id = insert(title, TaskOM.Status.OPEN, TaskOM.Priority.LOW);

        assertTrue(taskDao.findTasksByStatus(0).stream().anyMatch(t -> t.getId() == id));
    }
}
