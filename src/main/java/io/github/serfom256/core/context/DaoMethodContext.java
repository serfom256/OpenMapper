package io.github.serfom256.core.context;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import io.github.serfom256.parser.model.SQLRecord;

public class DaoMethodContext {

    private final ConcurrentSkipListSet<Integer> daoMethods = new ConcurrentSkipListSet<>();
    private final ConcurrentHashMap<String, SQLRecord> cachedProcedures = new ConcurrentHashMap<>();

    public void registerMethod(Method method) {
        daoMethods.add(getHash(method));
    }

    public void registerSqlProcedure(String procedure, SQLRecord sqlRecord) {
        cachedProcedures.put(procedure, sqlRecord);
    }

    public SQLRecord getSqlRecordByProcedure(String procedure) {
        return cachedProcedures.get(procedure);
    }

    public boolean hasEmbeddedSqlProcedure(Method method) {
        return daoMethods.contains(getHash(method));
    }

    private int getHash(Method method) {
        return Arrays.hashCode(method.getParameters()) * 31 + method.hashCode();
    }
}
