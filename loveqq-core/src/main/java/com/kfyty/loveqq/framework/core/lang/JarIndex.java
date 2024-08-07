package com.kfyty.loveqq.framework.core.lang;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * 描述: jar 索引
 *
 * @author kfyty725
 * @date 2023/3/15 19:10
 * @email kfyty725@hotmail.com
 */
public class JarIndex {
    /**
     * jar index 文件索引名称
     */
    public static final String JAR_INDEX_FILE_NAME = "jar.idx";

    /**
     * 启动 jar 包路径
     */
    private final String mainJarPath;

    /**
     * jar 文件 map
     */
    private final Map<String, JarFile> jarMap;

    /**
     * jar index of package mapping to jars
     */
    private final Map<String, List<String>> jarIndex;

    public JarIndex(String mainJarPath, Manifest manifest, InputStream jarIndex) {
        this.mainJarPath = mainJarPath;
        this.jarMap = new HashMap<>(256);
        this.jarIndex = new HashMap<>(256);
        this.loadJarFile(mainJarPath, manifest);
        this.loadJarIndex(mainJarPath, jarIndex);
    }

    /**
     * 获取所有的 jar url
     *
     * @return jar urls
     */
    public List<URL> getJarURLs() {
        return this.jarMap.values().stream().map(this::getJarURL).collect(Collectors.toList());
    }

    /**
     * 获取启动类所在的 jar 路径
     *
     * @return main class jar path
     */
    public String getMainJarPath() {
        return this.mainJarPath;
    }

    /**
     * 根据 JarFile 构建一个 URL
     *
     * @param jarFile Jar file
     * @return jar file url
     */
    @SneakyThrows(MalformedURLException.class)
    public URL getJarURL(JarFile jarFile) {
        return new URL("file", "", -1, '/' + jarFile.getName());                                   // 必须使用 file 协议，否则读取不到 resources
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param packageName 包名
     * @param jar         jar 路径
     * @param jarFile     jar 文件
     */
    public void addJarIndexMapping(String packageName, String jar, JarFile jarFile) {
        this.jarIndex.computeIfAbsent(packageName, k -> new LinkedList<>()).add(jar);
        this.jarMap.put(jar, jarFile);
    }

    public List<String> getJars(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return this.jarIndex.getOrDefault(name, Collections.emptyList());
        }
        String path = name.substring(0, lastDot).replace('.', '/');
        return this.jarIndex.getOrDefault(path, this.jarIndex.getOrDefault(name, Collections.emptyList()));
    }

    public List<JarFile> getJarFiles(String name) {
        List<String> jars = this.getJars(name);
        return jars.stream().map(this.jarMap::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @SneakyThrows(IOException.class)
    protected void loadJarFile(String mainJarPath, Manifest manifest) {
        String parentPath = Paths.get(mainJarPath).getParent().toString();
        String classpath = manifest.getMainAttributes().getValue("Class-Path");
        List<String> jarPaths = Arrays.stream(classpath.split(" ")).map(String::trim).collect(Collectors.toList());
        for (String jarPath : jarPaths) {
            this.jarMap.put(jarPath, new JarFile(new File(parentPath, jarPath)));
        }
    }

    @SneakyThrows(IOException.class)
    protected void loadJarIndex(String mainJarPath, InputStream jarIndex) {
        String line = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarIndex))) {
            while ((line = reader.readLine()) != null) {
                this.loadJarIndex(mainJarPath, line, reader);
            }
        }
    }

    protected void loadJarIndex(String mainJarPath, String currentLine, BufferedReader reader) throws IOException {
        if (!currentLine.endsWith(".jar")) {
            return;
        }

        String jar = currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.isEmpty() || currentLine.equals("\n") || currentLine.equals("\r\n")) {
                return;                                                                                                 // 当前 jar 索引处理完毕
            }
            this.jarIndex.computeIfAbsent(currentLine, k -> new LinkedList<>()).add(jar);
            if (mainJarPath.contains(jar)) {
                this.jarMap.put(jar, new JarFile(mainJarPath));
            }
        }
    }
}
