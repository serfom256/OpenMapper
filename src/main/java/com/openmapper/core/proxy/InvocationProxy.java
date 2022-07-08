package com.openmapper.core.proxy;


import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

public class InvocationProxy {

    public <T extends InvocationHandler> Object makeProxyFor(@NotNull T handler, @NotNull Type target) {
        try {
            return Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{Class.forName(target.getTypeName())}, handler);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T extends InvocationHandler> Object makeProxyFor(@NotNull T handler, @NotNull String target) {
        try {
            return Proxy.newProxyInstance(handler.getClass().getClassLoader(), new Class[]{Class.forName(target)}, handler);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
