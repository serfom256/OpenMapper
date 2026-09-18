package io.github.serfom256.unit.cache;

import io.github.serfom256.cache.QueryCacheImpl;
import io.github.serfom256.cache.QueryCacheKey;
import io.github.serfom256.core.query.model.QueryParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class QueryCacheImplTest {

    @Mock QueryParameters queryParameters1;
    @Mock QueryParameters queryParameters2;

    interface Dao {
        String find(long id);
        String findOther(long id);
    }

    private QueryCacheImpl cache;
    private Method find;
    private Method findOther;

    @BeforeEach
    void setUp() throws Exception {
        cache = new QueryCacheImpl();
        find = Dao.class.getDeclaredMethod("find", long.class);
        findOther = Dao.class.getDeclaredMethod("findOther", long.class);
    }

    private QueryCacheKey key(Method method, Object... args) {
        return new QueryCacheKey(method, args);
    }

    @Test
    void put_and_get_returnsStoredValue() {
        cache.put(key(find, 1L), queryParameters1);
        assertSame(queryParameters1, cache.get(key(find, 1L)));
    }

    @Test
    void get_nonExistentKey_returnsNull() {
        assertNull(cache.get(key(find, 999L)));
    }

    @Test
    @DisplayName("the same method with different arguments is a different entry")
    void differentArguments_areDistinctEntries() {
        cache.put(key(find, 1L), queryParameters1);
        cache.put(key(find, 2L), queryParameters2);
        assertSame(queryParameters1, cache.get(key(find, 1L)));
        assertSame(queryParameters2, cache.get(key(find, 2L)));
    }

    @Test
    @DisplayName("different methods with identical arguments are different entries")
    void differentMethods_areDistinctEntries() {
        cache.put(key(find, 1L), queryParameters1);
        cache.put(key(findOther, 1L), queryParameters2);
        assertSame(queryParameters1, cache.get(key(find, 1L)));
        assertSame(queryParameters2, cache.get(key(findOther, 1L)));
    }

    @Test
    void put_sameKey_overwritesPrevious() {
        cache.put(key(find, 1L), queryParameters1);
        cache.put(key(find, 1L), queryParameters2);
        assertSame(queryParameters2, cache.get(key(find, 1L)));
    }

    @Test
    @DisplayName("keys are value-equal, so a freshly built key finds the entry")
    void keysAreValueEqual() {
        assertEquals(key(find, 1L), key(find, 1L));
        assertEquals(key(find, 1L).hashCode(), key(find, 1L).hashCode());
        assertNotEquals(key(find, 1L), key(find, 2L));
        assertNotEquals(key(find, 1L), key(findOther, 1L));
    }

    @Test
    @DisplayName("entries expire")
    void expiredEntriesAreNotReturned() {
        QueryCacheImpl expiring = new QueryCacheImpl(100, -1L);
        expiring.put(key(find, 1L), queryParameters1);
        assertNull(expiring.get(key(find, 1L)));
    }

    @Test
    @DisplayName("the cache is bounded, so a hot loop cannot grow it without limit")
    void cacheIsBounded() {
        QueryCacheImpl bounded = new QueryCacheImpl(10, TimeUnit.MINUTES.toNanos(10));
        for (long i = 0; i < 1_000; i++) {
            bounded.put(key(find, i), queryParameters1);
        }
        int retained = 0;
        for (long i = 0; i < 1_000; i++) {
            if (bounded.get(key(find, i)) != null) {
                retained++;
            }
        }
        assertTrue(retained <= 10, "expected at most 10 retained entries, got " + retained);
    }
}
