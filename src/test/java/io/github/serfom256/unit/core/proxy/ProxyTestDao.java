package io.github.serfom256.unit.core.proxy;

public interface ProxyTestDao {

    String findById(long id);

    default String describe() {
        return "default:" + findById(1L);
    }
}
