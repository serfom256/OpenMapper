package io.github.serfom256.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class InvocationProxy {

    public <T extends InvocationHandler> Object makeProxyFor(T handler, Type target) {
        return makeProxyFor(handler, target.getTypeName());
    }

    public <T extends InvocationHandler> Object makeProxyFor(T handler, String target) {
        try {
            return Proxy.newProxyInstance(
                    handler.getClass().getClassLoader(),
                    new Class[]{Class.forName(target)},
                    handler);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot create DAO proxy for '" + target + "': class not found", e);
        }
    }
}
