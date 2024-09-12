package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.lang.task.BuildJarIndexAntTask;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 描述: jar 索引
 *
 * @author kfyty725
 * @date 2023/3/15 19:10
 * @email kfyty725@hotmail.com
 */
@Getter
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
     * jar index of package mapping to jars
     */
    private final Map<String, List<String>> jarIndex;

    /**
     * 构造器
     *
     * @param mainJarPath 启动 jar 包路径，也可以是开发集成环境的启动 class 路径
     * @param jarIndex    jar index 数据流
     */
    public JarIndex(String mainJarPath, InputStream jarIndex) {
        this.mainJarPath = mainJarPath;
        this.jarIndex = new ConcurrentHashMap<>(256, 0.95F);
        this.loadJarIndex(mainJarPath, jarIndex);
    }

    /**
     * 返回是否是从开发集成环境启动
     *
     * @return true if from IDE started
     */
    public boolean isExploded() {
        return !this.mainJarPath.endsWith(".jar");
    }

    /**
     * 获取所有的 jar url
     *
     * @return jar urls
     */
    public List<URL> getJarURLs() {
        return this.jarIndex.values().stream().flatMap(Collection::stream).map(this::getJarURL).collect(Collectors.toList());
    }

    /**
     * 获取 jar index
     *
     * @return jar index
     */
    public Map<String, List<String>> getJarIndex() {
        return Collections.unmodifiableMap(this.jarIndex);
    }

    /**
     * 根据 JarFile 构建一个 URL
     *
     * @param jarFile Jar file
     * @return jar file url
     */
    public URL getJarURL(JarFile jarFile) {
        return this.getJarURL(jarFile.getName());
    }

    /**
     * 根据 JarFile 构建一个 URL
     *
     * @param jarFilePath Jar file path
     * @return jar file url
     */
    @SuppressWarnings("deprecation")
    @SneakyThrows(MalformedURLException.class)
    public URL getJarURL(String jarFilePath) {
        return new URL("file", "", -1, '/' + jarFilePath);                                         // 必须使用 file 协议，否则读取不到 resources
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param jarFiles jar 文件集合
     */
    @SneakyThrows(Exception.class)
    public void addJarIndex(List<JarFile> jarFiles) {
        Map<String, Set<String>> indexContainer = new HashMap<>(jarFiles.size());
        for (JarFile jarFile : jarFiles) {
            BuildJarIndexAntTask.scanJarIndex(jarFile.getName(), jarFile, indexContainer);
        }
        String index = BuildJarIndexAntTask.buildJarIndex(indexContainer);
        this.loadJarIndex(this.mainJarPath, new ByteArrayInputStream(index.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param packageName 包名
     * @param jarFile     jar 文件
     */
    public void addJarIndex(String packageName, JarFile jarFile) {
        this.addJarIndex(packageName, jarFile.getName());
    }

    /**
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param packageName 包名
     * @param jarFilePath jar 文件绝对路径
     */
    public void addJarIndex(String packageName, String jarFilePath) {
        this.jarIndex.computeIfAbsent(packageName, k -> new LinkedList<>()).add(jarFilePath);
    }

    /**
     * 根据资源获取所在 jar 包集合
     *
     * @param name 资源名称
     * @return jars
     */
    public List<String> getJarFiles(String name) {
        int lastDot = name.lastIndexOf(name.endsWith(".class") ? '/' : '.');                                            // 为 com/kfyty/demo/Demo.class 提供支持
        if (lastDot < 0) {
            return this.jarIndex.getOrDefault(name, Collections.emptyList());
        }
        String path = name.substring(0, lastDot).replace('.', '/');
        List<String> jarFiles = this.jarIndex.get(path);
        return jarFiles != null ? jarFiles : this.jarIndex.getOrDefault(name, Collections.emptyList());
    }

    /**
     * 读取 jar index
     *
     * @param mainJarPath 启动类所在路径
     * @param jarIndex    jar index
     */
    @SneakyThrows(IOException.class)
    protected void loadJarIndex(String mainJarPath, InputStream jarIndex) {
        String line = null;
        String parentPath = Paths.get(mainJarPath).getParent().toString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarIndex))) {
            while ((line = reader.readLine()) != null) {
                this.loadJarIndex(parentPath, line, reader);
            }
        }
    }

    protected void loadJarIndex(String parentPath, String currentLine, BufferedReader reader) throws IOException {
        if (!currentLine.endsWith(".jar")) {
            return;
        }

        final String jar = currentLine.replace("%20", " ");                                            // 第一行是 jar 文件相对路径
        final File file = new File(jar);
        final String jarFile = file.exists() ? file.getAbsolutePath() : new File(parentPath, jar).getAbsolutePath();
        while ((currentLine = reader.readLine()) != null) {
            if (currentLine.isEmpty() || currentLine.equals("\n") || currentLine.equals("\r\n")) {
                return;                                                                                                 // 当前 jar 索引处理完毕
            }
            this.addJarIndex(currentLine.intern(), jarFile.intern());
        }
    }
}
