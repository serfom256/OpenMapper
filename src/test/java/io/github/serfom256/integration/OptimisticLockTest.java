package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.VersionedItemOM;
import io.github.serfom256.exceptions.internal.OptimisticLockException;

class OptimisticLockTest extends IntegrationTestBase {

    private VersionedItemOM insert(String name) {
        VersionedItemOM item = new VersionedItemOM();
        item.setName(name);
        item.setVersion(0);
        versionedItemDao.insertVersionedItem(item);
        return versionedItemDao.findVersionedItemById(idOf(name));
    }

    private long idOf(String name) {
        try (var connection = getConnection();
             var statement = connection.prepareStatement(
                     "SELECT id FROM versioned_item WHERE name = ?")) {
            statement.setString(1, name);
            var rs = statement.executeQuery();
            rs.next();
            return rs.getLong(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("a single update increments the version")
    void singleUpdateIncrementsVersion() {
        VersionedItemOM item = insert("lock-" + UUID.randomUUID());
        assertEquals(0, item.getVersion());

        item.setName(item.getName() + "-updated");
        versionedItemDao.updateVersionedItem(item);

        assertEquals(1, versionedItemDao.findVersionedItemById(item.getId()).getVersion());
    }

    @Test
    @DisplayName("updating against a stale version is rejected")
    void staleUpdateIsRejected() {
        VersionedItemOM item = insert("stale-" + UUID.randomUUID());
        versionedItemDao.updateVersionedItem(item);

        VersionedItemOM stale = new VersionedItemOM();
        stale.setId(item.getId());
        stale.setName("written from a stale copy");
        stale.setVersion(0);

        assertThrows(OptimisticLockException.class, () -> versionedItemDao.updateVersionedItem(stale));
        assertEquals(item.getName(), versionedItemDao.findVersionedItemById(item.getId()).getName());
    }

    @Test
    @DisplayName("a batch update applies to every element and increments each version")
    void batchUpdateIncrementsEachVersion() {
        VersionedItemOM first = insert("batch-a-" + UUID.randomUUID());
        VersionedItemOM second = insert("batch-b-" + UUID.randomUUID());

        first.setName(first.getName() + "-x");
        second.setName(second.getName() + "-x");
        int affected = versionedItemDao.updateVersionedItems(List.of(first, second));

        assertEquals(2, affected);
        assertEquals(1, versionedItemDao.findVersionedItemById(first.getId()).getVersion());
        assertEquals(1, versionedItemDao.findVersionedItemById(second.getId()).getVersion());
    }

    @Test
    @DisplayName("one stale element fails the whole batch")
    void batchWithStaleElementIsRejected() {
        VersionedItemOM fresh = insert("batch-fresh-" + UUID.randomUUID());
        VersionedItemOM stale = insert("batch-stale-" + UUID.randomUUID());
        versionedItemDao.updateVersionedItem(stale);

        VersionedItemOM staleCopy = new VersionedItemOM();
        staleCopy.setId(stale.getId());
        staleCopy.setName("stale");
        staleCopy.setVersion(0);

        assertThrows(OptimisticLockException.class,
                () -> versionedItemDao.updateVersionedItems(List.of(fresh, staleCopy)));
    }
}
