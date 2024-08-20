package com.kfyty.loveqq.framework.core.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 描述: 类加载器工具
 *
 * @author kfyty725
 * @date 2023/3/15 19:51
 * @email kfyty725@hotmail.com
 */
public abstract class ClassLoaderUtil {
    /**
     * 返回类加载器
     *
     * @param clazz 调用 class
     * @return 类加载器
     */
    public static ClassLoader classLoader(Class<?> clazz) {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * 获取类路径下所有 jar URL
     *
     * @param classLoader class loader
     * @return jar urls
     */
    public static Set<URL> resolveAllClassPath(ClassLoader classLoader) {
        return resolveAllClassPath(classLoader, new HashSet<>());
    }

    /**
     * 获取类路径下所有 jar URL
     *
     * @param classLoader class loader
     * @param result      结果集合
     * @return jar urls
     */
    public static Set<URL> resolveAllClassPath(ClassLoader classLoader, Set<URL> result) {
        if (classLoader instanceof URLClassLoader) {
            result.addAll(Arrays.asList(((URLClassLoader) classLoader).getURLs()));
        }

        if (classLoader == ClassLoader.getSystemClassLoader()) {
            try {
                String javaClassPath = System.getProperty("java.class.path");
                String pathSeparator = System.getProperty("path.separator");
                for (String path : CommonUtil.split(javaClassPath, pathSeparator)) {
                    result.add(new File(path).toURI().toURL());
                }
            } catch (MalformedURLException e) {
                throw ExceptionUtil.wrap(e);
            }
        }

        if (classLoader != null) {
            resolveAllClassPath(classLoader.getParent(), result);
        }

        return result;
    }
}
