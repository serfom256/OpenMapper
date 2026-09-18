package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.PreparedStatement;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.AuditedNoteOM;

class InheritedFieldsTest extends IntegrationTestBase {

    private long idOf(String body) {
        try (var connection = getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("SELECT id FROM audited_note WHERE body = ?")) {
            statement.setString(1, body);
            var rs = statement.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("fields declared on a base class are written")
    void inheritedFieldsAreWritten() {
        String body = "inherited-" + UUID.randomUUID();
        AuditedNoteOM note = new AuditedNoteOM();
        note.setBody(body);
        note.setCreatedBy("alice");

        auditedNoteDao.insertAuditedNote(note);

        assertEquals("alice", auditedNoteDao.findAuditedNoteById(idOf(body)).getCreatedBy());
    }

    @Test
    @DisplayName("fields declared on a base class are read, including the inherited primary key")
    void inheritedFieldsAreRead() {
        String body = "inherited-read-" + UUID.randomUUID();
        AuditedNoteOM note = new AuditedNoteOM();
        note.setBody(body);
        note.setCreatedBy("bob");
        auditedNoteDao.insertAuditedNote(note);

        long id = idOf(body);
        AuditedNoteOM loaded = auditedNoteDao.findAuditedNoteById(id);

        assertEquals(id, loaded.getId());
        assertEquals("bob", loaded.getCreatedBy());
        assertEquals(body, loaded.getBody());
    }
}
