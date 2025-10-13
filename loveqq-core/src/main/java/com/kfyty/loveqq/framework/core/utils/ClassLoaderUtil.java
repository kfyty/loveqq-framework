package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.lang.ConstantConfig;
import com.kfyty.loveqq.framework.core.lang.JarIndex;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import com.kfyty.loveqq.framework.core.support.task.BuildJarIndexAntTask;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 描述: 类加载器工具
 * 这里不要使用 {@link lombok.extern.slf4j.Slf4j} 打印日志
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
     * 获取类加载器
     *
     * @param clazz 启动类
     * @return 类加载器
     */
    @SneakyThrows(Exception.class)
    @SuppressWarnings("deprecation")
    public static JarIndexClassLoader getIndexedClassloader(Class<?> clazz) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (isIndexedClassLoader(contextClassLoader)) {
            return (JarIndexClassLoader) contextClassLoader;
        }
        Set<URL> urls = resolveClassPath(contextClassLoader);
        List<String> classPath = urls.stream().map(PathUtil::getPath).map(Path::toString).collect(Collectors.toList());
        String index = BuildJarIndexAntTask.buildJarIndex(BuildJarIndexAntTask.scanJarIndex(classPath, new HashMap<>()));
        Path mainJarPath = Paths.get(clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        JarIndex jarIndex = new JarIndex(mainJarPath.toString(), new ByteArrayInputStream(index.getBytes(StandardCharsets.UTF_8)), classPath);
        return new JarIndexClassLoader(jarIndex, contextClassLoader);
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
    @SuppressWarnings("UrlHashCode")
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
                                if (classPath != null && !classPath.isEmpty()) {
                                    String[] nestedClassPath = classPath.split(" ");
                                    for (String url : nestedClassPath) {
                                        if (JarIndexClassLoader.isJavaSystemResource(url)) {
                                            continue;
                                        }
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
                throw new ResolvableException(e);
            }
        }

        if (classLoader != null) {
            resolveClassPath(classLoader.getParent(), result);
        }

        return result;
    }

    /**
     * 使用指定的类加载器启动应用
     *
     * @param classLoader 类加载器
     * @param target      实例，如果是 {@link Class} 类型，则表示静态方法调用
     * @param args        方法参数
     */
    @SneakyThrows(Exception.class)
    public static void invokeOnClassLoader(ClassLoader classLoader, Object target, String method, Object... args) {
        Thread.currentThread().setContextClassLoader(classLoader);

        Class<?> invokeClass = Class.forName(target instanceof Class<?> ? ((Class<?>) target).getName() : target.getClass().getName(), false, classLoader);

        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = Class.forName(args[i].getClass().getName(), false, classLoader);
            if (args[i] instanceof Class<?>) {
                args[i] = Class.forName(((Class<?>) args[i]).getName(), false, classLoader);
            }
        }

        Method invokeMethod = invokeClass.getMethod(method, argTypes);

        invokeMethod.invoke(target instanceof Class<?> ? null : target, args);
    }
}
