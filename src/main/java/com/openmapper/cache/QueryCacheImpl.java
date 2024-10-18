package com.openmapper.cache;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.openmapper.config.OpenMapperGlobalEnvironmentVariables;
import com.openmapper.core.query.model.QueryParameters;

@Component
public class QueryCacheImpl implements QueryCache {

    private final long evictionDelayInSeconds = 10000L;
    private final ConcurrentSkipListMap<Long, Integer> timeMap = new ConcurrentSkipListMap<>();
    private final ConcurrentHashMap<Integer, QueryParameters> cacheEntryMap = new ConcurrentHashMap<>();
    private final Thread evictionThread;
    private final OpenMapperGlobalEnvironmentVariables variables;

    private static final Logger logger = LoggerFactory.getLogger(QueryCacheImpl.class);

    public QueryCacheImpl(OpenMapperGlobalEnvironmentVariables variables) {
        this.variables = variables;
        this.evictionThread = new Thread(this::evictOutdatedEntries);
        init();
    }

    void init() {
        evictionThread.start();
    }

    @Override
    public void put(int hash, QueryParameters queryEntry) {
        timeMap.put(Instant.now().getEpochSecond(), hash);
        cacheEntryMap.put(hash, queryEntry);
    }

    @Override
    public QueryParameters get(int hash) {
        return cacheEntryMap.get(hash);
    }

    private boolean evictOutdatedEntries() {
        while (true) {
            Map.Entry<Long, Integer> lastEntry = timeMap.lastEntry();
            int evictedCount = 0;
            while (lastEntry != null
                    && (Instant.now().getEpochSecond() - lastEntry.getKey() > evictionDelayInSeconds)) {
                timeMap.pollLastEntry();
                lastEntry = timeMap.lastEntry();
                cacheEntryMap.remove(lastEntry.getValue());
            }

            if (variables.isLoggingEnabled()) {
                logger.trace("{} entries evicted from cache", evictedCount);
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
