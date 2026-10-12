package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.serfom256.benchmark.model.om.NullableBoxOM;
import io.github.serfom256.benchmark.model.om.PrimitiveBoxOM;

class NullColumnTest extends IntegrationTestBase {

    private NullableBoxOM stored(NullableBoxOM box) {
        box.setName("nulls-" + UUID.randomUUID());
        nullableBoxDao.insertNullableBox(box);
        return nullableBoxDao.findNullableBoxByName(box.getName());
    }

    @Test
    @DisplayName("a SQL NULL reads back as null for every wrapper type, not as zero or false")
    void sqlNullStaysNullForWrappers() {
        NullableBoxOM loaded = stored(new NullableBoxOM());

        assertNull(loaded.getIntValue(), "Integer");
        assertNull(loaded.getLongValue(), "Long");
        assertNull(loaded.getDoubleValue(), "Double");
        assertNull(loaded.getFloatValue(), "Float");
        assertNull(loaded.getShortValue(), "Short");
        assertNull(loaded.getByteValue(), "Byte");
        assertNull(loaded.getBoolValue(), "Boolean");
        assertNull(loaded.getStringValue(), "String");
    }

    @Test
    @DisplayName("a present value still reads back unchanged")
    void presentValuesAreUnaffected() {
        NullableBoxOM box = new NullableBoxOM();
        box.setIntValue(7);
        box.setLongValue(8L);
        box.setDoubleValue(1.5);
        box.setFloatValue(2.5f);
        box.setShortValue((short) 3);
        box.setByteValue((byte) 4);
        box.setBoolValue(true);
        box.setStringValue("set");

        NullableBoxOM loaded = stored(box);

        assertEquals(7, loaded.getIntValue());
        assertEquals(8L, loaded.getLongValue());
        assertEquals(1.5, loaded.getDoubleValue());
        assertEquals(2.5f, loaded.getFloatValue());
        assertEquals((short) 3, loaded.getShortValue());
        assertEquals((byte) 4, loaded.getByteValue());
        assertEquals(true, loaded.getBoolValue());
        assertEquals("set", loaded.getStringValue());
    }

    @Test
    @DisplayName("a zero value is not confused with a NULL")
    void zeroIsDistinctFromNull() {
        NullableBoxOM box = new NullableBoxOM();
        box.setIntValue(0);
        box.setBoolValue(false);

        NullableBoxOM loaded = stored(box);

        assertEquals(0, loaded.getIntValue());
        assertEquals(false, loaded.getBoolValue());
    }

    @Test
    @DisplayName("primitive fields keep the JDBC default, since they cannot hold null")
    void primitivesKeepJdbcDefaults() {
        NullableBoxOM box = new NullableBoxOM();
        box.setName("prims-" + UUID.randomUUID());
        nullableBoxDao.insertNullableBox(box);

        PrimitiveBoxOM loaded = nullableBoxDao.findPrimitiveBoxByName(box.getName());

        assertEquals(0, loaded.getIntValue());
        assertEquals(0L, loaded.getLongValue());
        assertEquals(0.0, loaded.getDoubleValue());
        assertFalse(loaded.getBoolValue());
    }

    @Test
    @DisplayName("a scalar return of SQL NULL is null, not zero")
    void scalarNullIsNull() {
        assertNull(nullableBoxDao.findMaxIntValueByName("no-such-row-" + UUID.randomUUID()));
    }

    @Test
    @DisplayName("a scalar return of a real value is unaffected")
    void scalarValueIsUnaffected() {
        NullableBoxOM box = new NullableBoxOM();
        box.setIntValue(42);
        NullableBoxOM loaded = stored(box);

        assertEquals(42, nullableBoxDao.findMaxIntValueByName(loaded.getName()));
    }
}
