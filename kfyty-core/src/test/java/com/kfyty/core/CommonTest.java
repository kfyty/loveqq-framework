package com.kfyty.core;

import com.kfyty.core.autoconfig.annotation.BootApplication;
import com.kfyty.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.IOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2023/3/22 21:06
 * @email kfyty725@hotmail.com
 */
public class CommonTest {

    @Test
    public void removePrefixTest() {
        String s = "/api/test/api";
        String removePrefix = CommonUtil.removePrefix("/api", s);
        Assert.assertEquals(removePrefix, "/test/api");
    }

    @Test
    public void weakTest() {
        Map<Class<?>, Integer> cache = new WeakConcurrentHashMap<>();
        cache.put(BootApplication.class, 1);
        System.out.println(cache.get(BootApplication.class));
        System.gc();
        System.out.println(cache.get(BootApplication.class));
    }

    @Test
    public void resolveAllClassPathTest() {
        Set<URL> urls = IOUtil.resolveAllClassPath(this.getClass().getClassLoader(), new HashSet<>());
        Set<URL> resources = new PathMatchingResourcePatternResolver(urls).findResources("*.properties");
        System.out.println(resources);
    }
}
