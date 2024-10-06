package com.openmapper.cache;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class QueryCacheImpl implements QueryCache {

    private final ConcurrentSkipListMap<Long, Object> concurrentMap = new ConcurrentSkipListMap<>();
    private final Thread evictionThread;
    private final long evictionDelayInSeconds;

    private static final Logger logger = LoggerFactory.getLogger(QueryCacheImpl.class);

    public QueryCacheImpl() {
        this.evictionDelayInSeconds = 10000L;
        this.evictionThread = new Thread(this::evictOutdatedEntries);
    }

    void init() {
        evictionThread.start();
    }

    public void put(Object o) {
        concurrentMap.put(Instant.now().getEpochSecond(), o);
    }

    private boolean evictOutdatedEntries() {
        while (true) {
            Map.Entry<Long, Object> lastEntry = concurrentMap.lastEntry();
            while (lastEntry != null
                    && (Instant.now().getEpochSecond() - lastEntry.getKey() > evictionDelayInSeconds)) {
                concurrentMap.pollLastEntry();
                lastEntry = concurrentMap.lastEntry();
            }

            try {
                Thread.sleep(evictionDelayInSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }

}
