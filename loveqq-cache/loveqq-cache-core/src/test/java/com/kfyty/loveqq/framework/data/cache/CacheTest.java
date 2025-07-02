package com.kfyty.loveqq.framework.data.cache;

import com.kfyty.loveqq.framework.core.autoconfig.env.PropertyContext;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.data.cache.core.Cache;
import com.kfyty.loveqq.framework.data.cache.core.DefaultCache;
import com.kfyty.loveqq.framework.data.cache.core.annotation.CacheClean;
import com.kfyty.loveqq.framework.data.cache.core.annotation.Cacheable;
import com.kfyty.loveqq.framework.data.cache.core.proxy.AbstractCacheInterceptorProxy;
import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2024/7/4 10:44
 * @email kfyty725@hotmail.com
 */
public class CacheTest {

    @Test
    public void cacheTest() {
        Cache cache = new DefaultCache();
        cache.put("test", 1);
        cache.put("test", 2, 2);
        CommonUtil.sleep(3000);
        Assertions.assertNull(cache.get("test"));
    }

    @Test
    public void resolveTest() {
        Map<String, Object> map = new HashMap<>();
        CacheProxy cacheProxy = new CacheProxy();
        map.put("var", new Val());
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${var.v}", map), "aaa:1");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}:bbb", map), "1:bbb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${var.v}:bbb", map), "aaa:1:bbb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${k.aaa}:${var.v}", map), "aaa:ka:1");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${k.aaa}${var.v}", map), "aaa:ka1");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${var.v}${k.aaa}", map), "aaa:1ka");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${var.v}:${k.aaa}", map), "aaa:1:ka");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:${var.v}:${k.aaa}:bbb", map), "aaa:1:ka:bbb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}:${k.aaa}", map), "1:ka");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${k.aaa}:${var.v}", map), "ka:1");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}", map), "1");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}${var.v2}", map), "12");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}${var.v2}${var.toString()}", map), "12val");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}:${var.v2}", map), "1:2");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${var.v}:${var.toString()}${var.v2}", map), "1:val2");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${k.aaa}", map), "ka");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${k.aaa}${k.bbb}", map), "kakb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${k.aaa}:${k.bbb}", map), "ka:kb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("${k.aaa}${var.v}${k.bbb}", map), "ka1kb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa", map), "aaa");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:bbb", map), "aaa:bbb");
        Assertions.assertEquals(cacheProxy.resolvePlaceholders("aaa:bbb$", map), "aaa:bbb$");
    }

    @Data
    private static class Val {
        int v = 1;
        int v2 = 2;

        @Override
        public String toString() {
            return "val";
        }
    }

    private static class CacheProxy extends AbstractCacheInterceptorProxy {

        public CacheProxy() {
            super((PropertyContext) Proxy.newProxyInstance(CacheProxy.class.getClassLoader(), new Class[]{PropertyContext.class}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if (args[0].toString().equals("k.aaa")) {
                        return "ka";
                    }
                    if (args[0].toString().equals("k.bbb")) {
                        return "kb";
                    }
                    return null;
                }
            }), null, null, null);
        }

        @Override
        protected Object around(String cacheableName, String cacheCleanName, Cacheable cacheable, CacheClean cacheClean, Lazy<Map<String, Object>> context, Method method, ProceedingJoinPoint pjp) throws Throwable {
            return null;
        }

        @Override
        public String resolvePlaceholders(String value, Map<String, Object> map) {
            return super.resolvePlaceholders(value, map);
        }
    }
}
