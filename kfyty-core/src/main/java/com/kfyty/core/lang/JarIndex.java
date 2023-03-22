package com.kfyty.core.lang;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
@Slf4j
public class JarIndex {
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
        this.jarMap = new ConcurrentHashMap<>();
        this.jarIndex = new ConcurrentHashMap<>();
        this.loadJarFile(mainJarPath, manifest);
        this.loadJarIndex(mainJarPath, jarIndex);
    }

    public List<URL> getJarURLs() {
        return this.jarMap.values().stream().map(this::getJarURL).collect(Collectors.toList());
    }

    public String getMainJarPath() {
        return this.mainJarPath;
    }

    @SneakyThrows(MalformedURLException.class)
    public URL getJarURL(JarFile jarFile) {
        return new URL("file", "", -1, '/' + jarFile.getName());                                   // 必须使用 file 协议，否则读取不到 resources
    }

    public List<String> getJars(String name) {
        int lastDot = name.lastIndexOf('.');
        if (lastDot < 0) {
            return Collections.emptyList();
        }
        String path = name.substring(0, lastDot).replace('.', '/');
        return this.jarIndex.getOrDefault(path, Collections.emptyList());
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(jarIndex));
        while ((line = reader.readLine()) != null) {
            this.loadJarIndex(mainJarPath, line, reader);
        }
    }

    @SneakyThrows(IOException.class)
    protected void loadJarIndex(String mainJarPath, String currentLine, BufferedReader reader) {
        if (!currentLine.endsWith(".jar")) {
            return;
        }

        String jar = currentLine;
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.equals("") || currentLine.equals("\n") || currentLine.equals("\r\n")) {
                return;                                                                                                 // 当前 jar 索引处理完毕
            }
            if (!currentLine.startsWith("META-INF")) {
                this.jarIndex.computeIfAbsent(currentLine, k -> new LinkedList<>()).add(jar);
                if (mainJarPath.contains(jar)) {
                    this.jarMap.put(jar, new JarFile(mainJarPath));
                }
            }
        }
    }
}
