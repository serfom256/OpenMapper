package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.JsonDocOM;
import io.github.serfom256.benchmark.model.om.JsonSpecOM;

class JsonColumnTest extends IntegrationTestBase {

    private JsonDocOM doc(String name) {
        JsonDocOM doc = new JsonDocOM();
        doc.setName(name);
        return doc;
    }

    private JsonDocOM roundTrip(JsonDocOM doc) {
        jsonDocDao.insertJsonDoc(doc);
        JsonDocOM loaded = jsonDocDao.findJsonDocByName(doc.getName());
        assertNotNull(loaded, "the row was not written");
        return loaded;
    }

    @Test
    @DisplayName("a map field is written as JSON and read back as a map")
    void mapRoundTrips() {
        JsonDocOM doc = doc("json-map-" + UUID.randomUUID());
        doc.setAttributes(Map.of("colour", "red", "size", "large"));

        JsonDocOM loaded = roundTrip(doc);

        assertEquals(Map.of("colour", "red", "size", "large"), loaded.getAttributes());
    }

    @Test
    @DisplayName("the element type of a generic collection survives the round trip")
    void genericCollectionRoundTrips() {
        JsonDocOM doc = doc("json-list-" + UUID.randomUUID());
        doc.setTags(List.of("alpha", "beta", "gamma"));

        JsonDocOM loaded = roundTrip(doc);

        assertEquals(List.of("alpha", "beta", "gamma"), loaded.getTags());
    }

    @Test
    @DisplayName("a nested object is rebuilt into its own type, not a map")
    void nestedObjectRoundTrips() {
        JsonDocOM doc = doc("json-spec-" + UUID.randomUUID());
        JsonSpecOM spec = new JsonSpecOM();
        spec.setVendor("acme");
        spec.setRevision(7);
        doc.setSpec(spec);

        JsonDocOM loaded = roundTrip(doc);

        assertNotNull(loaded.getSpec());
        assertEquals(JsonSpecOM.class, loaded.getSpec().getClass());
        assertEquals("acme", loaded.getSpec().getVendor());
        assertEquals(7, loaded.getSpec().getRevision());
    }

    @Test
    @DisplayName("a json field maps into a real jsonb column when the query casts it")
    void jsonbColumnRoundTrips() {
        JsonDocOM doc = doc("json-jsonb-" + UUID.randomUUID());
        doc.setPayload(Map.of("enabled", true, "retries", 3));

        JsonDocOM loaded = roundTrip(doc);

        assertEquals(true, loaded.getPayload().get("enabled"));
        assertEquals(3, loaded.getPayload().get("retries"));
    }

    @Test
    @DisplayName("a null json field stays null rather than becoming the string \"null\"")
    void nullStaysNull() {
        JsonDocOM loaded = roundTrip(doc("json-null-" + UUID.randomUUID()));

        assertNull(loaded.getAttributes());
        assertNull(loaded.getTags());
        assertNull(loaded.getSpec());
        assertNull(loaded.getPayload());
    }

    @Test
    @DisplayName("every json field on one entity is handled independently")
    void severalJsonFieldsTogether() {
        JsonDocOM doc = doc("json-all-" + UUID.randomUUID());
        doc.setAttributes(Map.of("k", "v"));
        doc.setTags(List.of("one"));
        JsonSpecOM spec = new JsonSpecOM();
        spec.setVendor("multi");
        spec.setRevision(1);
        doc.setSpec(spec);
        doc.setPayload(Map.of("n", 1));

        JsonDocOM loaded = roundTrip(doc);

        assertEquals(Map.of("k", "v"), loaded.getAttributes());
        assertEquals(List.of("one"), loaded.getTags());
        assertEquals("multi", loaded.getSpec().getVendor());
        assertEquals(1, loaded.getPayload().get("n"));
    }
}
