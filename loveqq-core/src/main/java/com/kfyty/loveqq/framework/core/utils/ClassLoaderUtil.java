package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

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
     * 是否是 jar index 支持的类加载器
     *
     * @return true if jar index supported
     */
    public static boolean isIndexedClassLoader() {
        return isIndexedClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 是否是 jar index 支持的类加载器
     *
     * @param classLoader 类加载器
     * @return true if jar index supported
     */
    public static boolean isIndexedClassLoader(ClassLoader classLoader) {
        return classLoader.getClass().getName().equals(JarIndexClassLoader.class.getName());
    }

    /**
     * 获取类路径下所有 jar URL
     *
     * @param classLoader class loader
     * @return jar urls
     */
    public static Set<URL> resolveClassPath(ClassLoader classLoader) {
        return resolveClassPath(classLoader, new HashSet<>());
    }

    /**
     * 获取类路径下所有 jar URL
     *
     * @param classLoader class loader
     * @param result      结果集合
     * @return jar urls
     */
    @SuppressWarnings({"UrlHashCode", "SizeReplaceableByIsEmpty"})
    public static Set<URL> resolveClassPath(ClassLoader classLoader, Set<URL> result) {
        if (classLoader instanceof URLClassLoader) {
            result.addAll(Arrays.asList(((URLClassLoader) classLoader).getURLs()));

            // JarIndexClassLoader 已加载全部的，可以直接返回
            if (classLoader instanceof JarIndexClassLoader) {
                return result;
            }
        }

        if (classLoader == ClassLoader.getSystemClassLoader()) {
            String javaClassPath = System.getProperty("java.class.path");
            String[] classPathArr = javaClassPath.split(File.pathSeparator);
            try {
                for (String path : classPathArr) {
                    // 添加当前 path
                    File file = new File(path);
                    result.add(file.toURI().toURL());

                    // 解析 jar 内的 class-path，主要是 ide short commandline 支持
                    if (classPathArr.length == 1 || ConstantConfig.LOAD_JAR_CLASS_PATH) {
                        if (file.isFile()) {
                            try (JarFile jarFile = new JarFile(file)) {
                                String classPath = jarFile.getManifest().getMainAttributes().getValue("Class-Path");
                                if (classPath != null && classPath.length() > 0) {
                                    String[] nestedClassPath = classPath.split(" ");
                                    for (String url : nestedClassPath) {
                                        URI uri = URI.create(url);
                                        if (uri.isAbsolute()) {
                                            result.add(uri.toURL());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw ExceptionUtil.wrap(e);
            }
        }

        if (classLoader != null) {
            resolveClassPath(classLoader.getParent(), result);
        }

        return result;
    }
}
