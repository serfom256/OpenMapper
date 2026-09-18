package io.github.serfom256.unit.daobeans;

public class EarlyConsumer {

    private final EarlyDao dao;

    public EarlyConsumer(EarlyDao dao) {
        this.dao = dao;
    }

    public EarlyDao dao() {
        return dao;
    }
}
