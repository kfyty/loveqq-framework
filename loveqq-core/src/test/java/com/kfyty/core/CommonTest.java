package com.kfyty.core;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        Assertions.assertEquals(removePrefix, "/test/api");
    }

    @Test
    public void resolveURLParamTest() {
        Map<String, String> map = CommonUtil.resolveURLParameters("http://aaa.com/aaa?p.a=1");
        Map<String, String> map2 = CommonUtil.resolveURLParameters("a=1", "p");
        Assertions.assertEquals(map, map2);
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
    public void iteratorSplitTest() {
        String s = "<a><b><c>";
        List<String> r = new ArrayList<>();
        CommonUtil.iteratorSplit(s, "<", ">", r::add);
        Assertions.assertEquals(r, Arrays.asList("a", "b", "c"));
    }

    @Test
    public void resolveAllClassPathTest() {
        Set<URL> urls = IOUtil.resolveAllClassPath(this.getClass().getClassLoader(), new HashSet<>());
        Set<URL> resources = new PathMatchingResourcePatternResolver(urls).findResources("*.properties");
        System.out.println(resources);
    }
}
