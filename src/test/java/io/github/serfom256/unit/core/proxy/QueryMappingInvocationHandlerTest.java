package io.github.serfom256.unit.core.proxy;

import io.github.serfom256.core.proxy.QueryMappingInvocationHandler;
import io.github.serfom256.core.query.facade.QueryFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryMappingInvocationHandlerTest {

    @Mock QueryFacade facade;
    @Mock DataSource dataSource;

    interface TestDao {
        String findById(long id);
    }

    @Test
    void invoke_delegatesToFacade() throws Throwable {
        QueryMappingInvocationHandler handler = new QueryMappingInvocationHandler(facade, dataSource);
        Method m = TestDao.class.getDeclaredMethod("findById", long.class);
        Object[] args = {42L};

        when(facade.invokeMethodQueryWithParameters(m, args, dataSource)).thenReturn("result");

        Object result = handler.invoke(null, m, args);
        assertEquals("result", result);
        verify(facade).invokeMethodQueryWithParameters(m, args, dataSource);
    }

    @Test
    void invoke_nullArgs_delegatesToFacade() throws Throwable {
        QueryMappingInvocationHandler handler = new QueryMappingInvocationHandler(facade, dataSource);
        Method m = TestDao.class.getDeclaredMethod("findById", long.class);

        when(facade.invokeMethodQueryWithParameters(m, null, dataSource)).thenReturn("no-args");

        Object result = handler.invoke(null, m, null);
        assertEquals("no-args", result);
    }

    @Test
    void invoke_objectMethods_doNotReachTheFacade() throws Throwable {
        QueryMappingInvocationHandler handler = new QueryMappingInvocationHandler(facade, dataSource);
        TestDao proxy = (TestDao) new io.github.serfom256.core.proxy.InvocationProxy()
                .makeProxyFor(handler, TestDao.class.getName());

        assertEquals(proxy, proxy);
        assertNotEquals(proxy, new Object());
        assertEquals(System.identityHashCode(proxy), proxy.hashCode());
        assertTrue(proxy.toString().contains("TestDao"));
        verifyNoInteractions(facade);
    }

    @Test
    void invoke_defaultMethod_runsTheInterfaceBody() throws Throwable {
        QueryMappingInvocationHandler handler = new QueryMappingInvocationHandler(facade, dataSource);
        ProxyTestDao proxy = (ProxyTestDao) new io.github.serfom256.core.proxy.InvocationProxy()
                .makeProxyFor(handler, ProxyTestDao.class.getName());

        when(facade.invokeMethodQueryWithParameters(any(), any(), eq(dataSource))).thenReturn("x");

        assertEquals("default:x", proxy.describe());
    }
}
