package com.kfyty.loveqq.framework.core.io;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import com.kfyty.loveqq.framework.core.utils.PropertiesUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 描述: spi 工厂加载机制
 *
 * @author kfyty725
 * @date 2021/7/14 11:24
 * @email kfyty725@hotmail.com
 */
public abstract class FactoriesLoader {
    public static final String DEFAULT_FACTORIES_RESOURCE_LOCATION = "META-INF/k.factories";

    private static final Map<String, Set<Properties>> loadedCache = new WeakConcurrentHashMap<>(4);

    private static final Map<String, Set<String>> factoriesCache = new WeakConcurrentHashMap<>(4);

    public static Set<String> loadFactories(Class<?> clazz) {
        return loadFactories(clazz.getName());
    }

    public static Set<String> loadFactories(String key) {
        return loadFactories(key, DEFAULT_FACTORIES_RESOURCE_LOCATION);
    }

    public static Set<String> loadFactories(Class<?> clazz, String factoriesResourceLocation) {
        return loadFactories(clazz.getName(), factoriesResourceLocation);
    }

    public static Set<String> loadFactories(String key, String factoriesResourceLocation) {
        return factoriesCache.computeIfAbsent(key, k -> {
            Set<String> factories = new HashSet<>();
            Set<Properties> properties = loadedCache.getOrDefault(factoriesResourceLocation, loadFactoriesResource(factoriesResourceLocation));
            for (Properties property : properties) {
                for (Map.Entry<Object, Object> entry : property.entrySet()) {
                    if (entry.getKey().toString().equals(key)) {
                        factories.addAll(Arrays.asList(entry.getValue().toString().split(",")));
                    }
                }
            }
            return factories;
        });
    }

    public static Set<Properties> loadFactoriesResource(String factoriesResourceLocation) {
        return loadedCache.computeIfAbsent(factoriesResourceLocation, k -> {
            try {
                Set<Properties> properties = new HashSet<>();
                Set<URL> urls = loadURLResource(factoriesResourceLocation);
                for (URL url : urls) {
                    properties.add(PropertiesUtil.load(url.openStream()));
                }
                return properties;
            } catch (IOException e) {
                throw new ResolvableException("unable to load factories from location [" + factoriesResourceLocation + "]", e);
            }
        });
    }

    public static Set<URL> loadURLResource(String urlResourceLocation) {
        return loadURLResource(urlResourceLocation, e -> true);
    }

    public static Set<URL> loadURLResource(String urlResourceLocation, Predicate<URL> filter) {
        try {
            Set<URL> retValue = new HashSet<>();
            Enumeration<URL> urls = ClassLoaderUtil.classLoader(FactoriesLoader.class).getResources(urlResourceLocation);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (filter.test(url)) {
                    retValue.add(url);
                }
            }
            return retValue;
        } catch (IOException e) {
            throw new ResolvableException("unable to load resource from location [" + urlResourceLocation + "]", e);
        }
    }
}
