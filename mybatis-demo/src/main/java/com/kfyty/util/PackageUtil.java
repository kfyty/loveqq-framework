package com.kfyty.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 功能描述: 解析 package 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 14:17
 * @since JDK 1.8
 */
public class PackageUtil {
    public static Set<Class<?>> parseBasePackage(String packageName) throws IOException, ClassNotFoundException {
        return parseBasePackage(packageName, true);
    }

    public static Set<Class<?>> parseBasePackage(String basePackage, boolean containChild) throws IOException, ClassNotFoundException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(basePackage.replace(".", "/"));
        return url == null ? null :
                url.getProtocol().equalsIgnoreCase("jar") ?
                parseBasePackageByJar(url.getPath(), containChild) :
                parseBasePackageByFile(url.getPath(), containChild);
    }

    private static Set<Class<?>> parseBasePackageByJar(String jarURL, boolean containChild) throws IOException, ClassNotFoundException {
        Set<Class<?>> set = new HashSet<>();
        String[] jarInfo = jarURL.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        String packagePath = jarInfo[1].substring(1);
        for(Enumeration<JarEntry> entries = new JarFile(jarFilePath).entries(); entries.hasMoreElements(); ) {
            String entryName = entries.nextElement().getName();
            if (containChild) {
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                    set.add(Class.forName(className));
                }
                continue;
            }
            int index = entryName.lastIndexOf("/");
            String tempPath = index == -1 ? entryName : entryName.substring(0, index);
            if (tempPath.equals(packagePath) && entryName.endsWith(".class")) {
                String className = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                set.add(Class.forName(className));
            }
        }
        return set;
    }

    private static Set<Class<?>> parseBasePackageByFile(String directoryPath, boolean containChild) throws ClassNotFoundException {
        Set<Class<?>> set = new HashSet<>();
        File[] files = new File(directoryPath).listFiles();
        for(File file : files) {
            if(file.isDirectory()) {
                if(containChild) {
                    set.addAll(parseBasePackageByFile(file.getPath(), true));
                }
                continue;
            }
            String classPath = file.getPath();
            if(classPath.endsWith(".class")) {
                classPath = classPath.substring(classPath.indexOf(File.separator + "classes") + 9, classPath.lastIndexOf("."));
                classPath = classPath.replace(File.separator, ".");
                set.add(Class.forName(classPath));
            }
        }
        return set;
    }
}
