package io.github.serfom256.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import io.github.serfom256.core.query.model.QueryParameters;

public class QueryCacheImpl implements QueryCache {

    private static final int MAX_ENTRIES = 10_000;
    private static final long TTL_NANOS = TimeUnit.MINUTES.toNanos(10);

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<QueryCacheKey, CacheEntry> entries;
    private final long ttlNanos;

    public QueryCacheImpl() {
        this(MAX_ENTRIES, TTL_NANOS);
    }

    public QueryCacheImpl(int maxEntries, long ttlNanos) {
        this.ttlNanos = ttlNanos;
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<QueryCacheKey, CacheEntry> eldest) {
                return size() > maxEntries;
            }
        };
    }

    @Override
    public void put(QueryCacheKey key, QueryParameters queryEntry) {
        lock.lock();
        try {
            entries.put(key, new CacheEntry(queryEntry, System.nanoTime() + ttlNanos));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public QueryParameters get(QueryCacheKey key) {
        lock.lock();
        try {
            CacheEntry entry = entries.get(key);
            if (entry == null) {
                return null;
            }
            if (isExpired(entry)) {
                entries.remove(key);
                return null;
            }
            return entry.value;
        } finally {
            lock.unlock();
        }
    }

    private boolean isExpired(CacheEntry entry) {
        return System.nanoTime() - entry.expiresAtNanos >= 0;
    }

    private static final class CacheEntry {
        private final QueryParameters value;
        private final long expiresAtNanos;

        private CacheEntry(QueryParameters value, long expiresAtNanos) {
            this.value = value;
            this.expiresAtNanos = expiresAtNanos;
        }
    }
}
