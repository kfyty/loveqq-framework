package com.kfyty.support.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

/**
 * 功能描述: 解析 package 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 14:17
 * @since JDK 1.8
 */
@Slf4j
public abstract class PackageUtil {
    public static Set<String> scanClassName(Class<?> mainClass) throws IOException {
        return scanClassName(mainClass.getPackage().getName());
    }

    public static Set<Class<?>> scanClass(Class<?> mainClass) throws IOException {
        return scanClass(mainClass.getPackage().getName());
    }

    public static Set<String> scanClassName(String basePackage) throws IOException {
        Set<String> classes = new HashSet<>();
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(basePackage.replace(".", "/"));
        while(urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if("jar".equalsIgnoreCase(url.getProtocol())) {
                classes.addAll(scanClassNameByJar(url));
                continue;
            }
            classes.addAll(scanClassNameByFile(url));
        }
        return classes;
    }

    public static Set<Class<?>> scanClass(String basePackage) throws IOException {
        Set<Class<?>> result = new HashSet<>();
        Set<String> classes = scanClassName(basePackage);
        if(CommonUtil.empty(classes)) {
            return result;
        }
        for (String clazz : classes) {
            try {
                result.add(Class.forName(clazz));
            } catch (Throwable e) {
                log.error("load class error !", e);
            }
        }
        return result;
    }

    private static Set<String> scanClassNameByJar(URL url) throws IOException {
        Set<String> classes = new HashSet<>();
        String path = !url.getPath().contains("!") ? url.getPath() : url.getPath().split("!")[1];
        Enumeration<JarEntry> entries = ((JarURLConnection) url.openConnection()).getJarFile().entries();
        while(entries.hasMoreElements()) {
            String classPath = entries.nextElement().getName();
            if(!("/" + classPath).startsWith(path) || !classPath.endsWith(".class")) {
                continue;
            }
            classes.add(classPath.replace("/", ".").replace(".class", ""));
        }
        return classes;
    }

    private static Set<String> scanClassNameByFile(URL url) throws MalformedURLException {
        Set<String> classes = new HashSet<>();
        File[] files = new File(url.getPath()).listFiles();
        if(CommonUtil.empty(files)) {
            return classes;
        }
        for(File file : files) {
            if(file.isDirectory()) {
                classes.addAll(scanClassNameByFile(file.toURI().toURL()));
                continue;
            }
            if(!file.getPath().endsWith(".class")) {
                continue;
            }
            String classPath = file.getPath();
            classPath = classPath.substring(classPath.indexOf("classes" + File.separator) + 8, classPath.lastIndexOf("."));
            classes.add(classPath.replace(File.separator, "."));
        }
        return classes;
    }
}
