package com.kfyty.support.io;

import com.kfyty.support.exception.SupportException;
import com.kfyty.support.utils.CommonUtil;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 描述: spi 工厂加载机制
 *
 * @author kfyty725
 * @date 2021/7/14 11:24
 * @email kfyty725@hotmail.com
 */
public abstract class FactoriesLoader {
    public static final String DEFAULT_FACTORIES_RESOURCE_LOCATION = "META-INF/k.factories";

    private static final Map<String, Set<Properties>> loadedCache = Collections.synchronizedMap(new WeakHashMap<>(4));

    private static final Map<String, Set<String>> factoriesCache = Collections.synchronizedMap(new WeakHashMap<>(4));

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
        if(factoriesCache.containsKey(key)) {
            return factoriesCache.get(key);
        }
        Set<String> factories = new HashSet<>();
        Set<Properties> properties = loadedCache.getOrDefault(factoriesResourceLocation, loadFactoriesResource(factoriesResourceLocation));
        for (Properties property : properties) {
            for (Map.Entry<Object, Object> entry : property.entrySet()) {
                if (entry.getKey().toString().equals(key)) {
                    factories.addAll(CommonUtil.split(entry.getValue().toString(), ","));
                }
            }
        }
        factoriesCache.put(key, factories);
        return factories;
    }

    public static Set<Properties> loadFactoriesResource(String factoriesResourceLocation) {
        try {
            Set<Properties> properties = new HashSet<>();
            Enumeration<URL> urls = FactoriesLoader.class.getClassLoader().getResources(factoriesResourceLocation);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                Properties prop = new Properties();
                prop.load(url.openStream());
                properties.add(prop);
            }
            loadedCache.put(factoriesResourceLocation, properties);
            return properties;
        } catch (Exception e) {
            throw new SupportException("unable to load factories from location [" + factoriesResourceLocation + "]", e);
        }
    }
}
