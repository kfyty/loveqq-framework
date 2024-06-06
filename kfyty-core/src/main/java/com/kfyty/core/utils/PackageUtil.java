package com.kfyty.core.utils;

import com.kfyty.core.lang.util.concurrent.WeakConcurrentHashMap;
import com.kfyty.core.support.EnumerationIterator;
import com.kfyty.core.support.io.PathMatchingResourcePatternResolver;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.CommonUtil.EMPTY_STRING;
import static com.kfyty.core.utils.ReflectUtil.isAbstract;

/**
 * 功能描述: 解析 package 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 14:17
 * @since JDK 1.8
 */
@Slf4j
public abstract class PackageUtil {
    private static final Map<String, Set<String>> SCAN_PACKAGE_CACHE = new WeakConcurrentHashMap<>(4);

    public static Set<String> scanClassName(Class<?> mainClass) {
        return scanClassName(mainClass.getPackage().getName());
    }

    public static Set<Class<?>> scanClass(Class<?> mainClass) {
        return scanClass(mainClass.getPackage().getName());
    }

    public static <T> List<T> scanInstance(Class<T> mainClass) {
        return scanInstance(mainClass, clazz -> !isAbstract(clazz) && mainClass.isAssignableFrom(clazz));
    }

    public static <T> List<T> scanInstance(Class<T> mainClass, Predicate<Class<?>> scanFilter) {
        return scanInstance(mainClass.getPackage().getName(), scanFilter);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> scanInstance(String basePackage, Predicate<Class<?>> scanFilter) {
        Set<Class<?>> classes = PackageUtil.scanClass(basePackage);
        return (List<T>) classes.stream().filter(scanFilter).map(ReflectUtil::newInstance).collect(Collectors.toList());
    }

    public static Set<Class<?>> scanClass(String basePackage) {
        return scanClass(basePackage, null);
    }

    public static Set<Class<?>> scanClass(String basePackage, PathMatchingResourcePatternResolver resolver) {
        Set<Class<?>> result = new HashSet<>();
        Set<String> classes = scanClassName(basePackage, resolver);
        if (CommonUtil.empty(classes)) {
            return result;
        }
        for (String clazz : classes) {
            Optional.ofNullable(ReflectUtil.load(clazz, false, false)).ifPresent(result::add);
        }
        return result;
    }

    public static Set<String> scanClassName(String basePackage) {
        return scanClassName(basePackage, null);
    }

    public static Set<String> scanClassName(String basePackage, PathMatchingResourcePatternResolver resolver) {
        try {
            Set<String> cache = SCAN_PACKAGE_CACHE.get(basePackage);
            if (cache != null) {
                return cache;
            }
            Set<String> classes = new HashSet<>();
            Iterable<URL> urls = resolver != null && basePackage.contains("*")
                    ? resolver.findResources(basePackage.replace('.', '/') + ".class")
                    : new EnumerationIterator<>(ClassLoaderUtil.classLoader(PackageUtil.class).getResources(basePackage.replace('.', '/')));
            for (URL url : urls) {
                if ("jar".equalsIgnoreCase(url.getProtocol())) {
                    classes.addAll(scanClassNameByJar(url));
                    continue;
                }
                classes.addAll(scanClassNameByFile(url));
            }
            return SCAN_PACKAGE_CACHE.computeIfAbsent(basePackage, k -> classes);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    private static Set<String> scanClassNameByJar(URL url) {
        try {
            Set<String> classes = new HashSet<>();
            JarURLConnection urlConnection = ((JarURLConnection) url.openConnection());
            if (urlConnection.getEntryName().endsWith(".class")) {
                classes.add(urlConnection.getEntryName().replace('/', '.').replace(".class", EMPTY_STRING));
                return classes;
            }
            String path = url.getPath().contains("!") ? url.getPath().split("!")[1] : url.getPath();
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }
            Enumeration<JarEntry> entries = urlConnection.getJarFile().entries();
            while (entries.hasMoreElements()) {
                String classPath = entries.nextElement().getName();
                if (!classPath.startsWith(path) || !classPath.endsWith(".class")) {
                    continue;
                }
                classes.add(classPath.replace('/', '.').replace(".class", EMPTY_STRING));
            }
            return classes;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    private static Set<String> scanClassNameByFile(URL url) {
        try {
            Set<String> classes = new HashSet<>();
            File urlFile = new File(url.getPath());
            File[] files = urlFile.listFiles();
            if (CommonUtil.empty(files)) {
                if (urlFile.getPath().endsWith(".class")) {
                    String classPath = urlFile.getPath();
                    classPath = classPath.substring(classPath.indexOf("classes" + File.separator) + 8, classPath.lastIndexOf("."));
                    classes.add(classPath.replace(File.separator, "."));
                }
                return classes;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(scanClassNameByFile(file.toURI().toURL()));
                    continue;
                }
                if (!file.getPath().endsWith(".class")) {
                    continue;
                }
                String classPath = file.getPath();
                classPath = classPath.substring(classPath.indexOf("classes" + File.separator) + 8, classPath.lastIndexOf("."));
                classes.add(classPath.replace(File.separator, "."));
            }
            return classes;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
