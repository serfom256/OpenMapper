package io.github.serfom256.unit.common.reflect;

import io.github.serfom256.common.reflect.FieldValueGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldValueGeneratorTest {

    private final FieldValueGenerator generator = new FieldValueGenerator();

    @Test
    void nullValue_returnsZero() {
        assertEquals(0, generator.getNextOptimisticLockFieldValue(null));
    }

    @Test
    void integerValue_incrementsByOne() {
        assertEquals(2, (int) generator.getNextOptimisticLockFieldValue(1));
        assertEquals(1, (int) generator.getNextOptimisticLockFieldValue(0));
        assertEquals(100, (int) generator.getNextOptimisticLockFieldValue(99));
    }

    @Test
    void longValue_incrementsByOne() {
        assertEquals(2L, (long) generator.getNextOptimisticLockFieldValue(1L));
        assertEquals(1L, (long) generator.getNextOptimisticLockFieldValue(0L));
    }

    @Test
    void shortValue_incrementsByOne() {
        assertEquals((short) 2, (short) generator.getNextOptimisticLockFieldValue((short) 1));
        assertEquals((short) 1, (short) generator.getNextOptimisticLockFieldValue((short) 0));
    }

    @Test
    void unsupportedType_throws() {
        assertThrows(NullPointerException.class,
                () -> generator.getNextOptimisticLockFieldValue(1.0));
    }
}
