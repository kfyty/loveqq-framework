package com.kfyty.core;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.BootApplication;
import com.kfyty.loveqq.framework.core.lang.Value;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.support.io.PathMatchingResourcePatternResolver;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.ConverterUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.annotation.RetentionPolicy;
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
    public void formatTest() {
        String format = CommonUtil.format("{}{}{}", 1, 2, 3);
        String format2 = CommonUtil.format("hello{} {}", ",", "world");
        Assertions.assertEquals(format, "123");
        Assertions.assertEquals(format2, "hello, world");
    }

    @Test
    public void enumConvertTest() {
        RetentionPolicy convert = ConverterUtil.convert("java.lang.annotation.RetentionPolicy.SOURCE", RetentionPolicy.class);
        Assertions.assertEquals(convert, RetentionPolicy.SOURCE);
    }

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
        CommonUtil.split(s, "<", ">", r::add);
        Assertions.assertEquals(r, Arrays.asList("a", "b", "c"));
    }

    @Test
    public void resolveAllClassPathTest() {
        Set<URL> urls = ClassLoaderUtil.resolveClassPath(this.getClass().getClassLoader(), new HashSet<>());
        Set<URL> resources = new PathMatchingResourcePatternResolver(urls).findResources("*.properties");
        System.out.println(resources);
    }

    @Test
    public void valueTest() {
        Assertions.assertEquals(new Value<>(new int[]{1, 2}), new Value<>(new int[]{1, 2}));
        Assertions.assertNotEquals(new Value<>(new int[]{1, 2}), new Value<>(new byte[]{1, 2}));
    }
}
