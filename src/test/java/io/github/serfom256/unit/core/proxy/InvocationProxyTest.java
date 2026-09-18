package io.github.serfom256.unit.core.proxy;

import io.github.serfom256.core.proxy.InvocationProxy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class InvocationProxyTest {

    private final InvocationProxy proxy = new InvocationProxy();

    interface TestInterface {
        String hello();
    }

    static class TestHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return "invoked";
        }
    }

    @Test
    void makeProxyFor_withType_returnsProxy() {
        TestHandler handler = new TestHandler();
        Object result = proxy.makeProxyFor(handler, TestInterface.class);
        assertNotNull(result);
        assertInstanceOf(TestInterface.class, result);
    }

    @Test
    void makeProxyFor_withTypeName_returnsProxy() {
        TestHandler handler = new TestHandler();
        Object result = proxy.makeProxyFor(handler, TestInterface.class.getName());
        assertNotNull(result);
        assertInstanceOf(TestInterface.class, result);
    }

    @Test
    void makeProxyFor_invalidClassName_failsFast() {
        TestHandler handler = new TestHandler();
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> proxy.makeProxyFor(handler, "com.nonexistent.FakeClass"));
        assertTrue(ex.getMessage().contains("com.nonexistent.FakeClass"));
        assertInstanceOf(ClassNotFoundException.class, ex.getCause());
    }

    @Test
    void proxy_delegatesToHandler() {
        TestHandler handler = new TestHandler();
        Object proxyObj = proxy.makeProxyFor(handler, TestInterface.class);
        TestInterface ti = (TestInterface) proxyObj;
        assertEquals("invoked", ti.hello());
    }
}
