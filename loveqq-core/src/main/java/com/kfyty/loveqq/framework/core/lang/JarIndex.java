package com.kfyty.loveqq.framework.core.lang;

import com.kfyty.loveqq.framework.core.support.task.BuildJarIndexAntTask;
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
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * 描述: jar 索引
 * 这里不要使用 {@link lombok.extern.slf4j.Slf4j} 打印日志
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
     * ide 启动类路径
     * 仅 ide 启动时有值
     */
    private final List<String> classpath;

    /**
     * jar index of package mapping to jars
     */
    private final Map<String, List<String>> jarIndex;

    /**
     * jar index 包含的全部的 URL
     */
    private URL[] urls;

    /**
     * 构造器
     * 这里使用 {@link ConcurrentSkipListMap} 更多是考虑空间利用率
     *
     * @param mainJarPath 启动 jar 包路径，也可以是开发集成环境的启动 class 路径
     * @param jarIndex    jar index 数据流
     */
    public JarIndex(String mainJarPath, InputStream jarIndex) {
        this(mainJarPath, jarIndex, null);
    }

    /**
     * 构造器，主要是支持 ide 启动
     *
     * @param mainJarPath 启动 jar 包路径，也可以是开发集成环境的启动 class 路径
     * @param jarIndex    jar index 数据流
     * @param classpath   类路径，ide 启动支持
     */
    @Deprecated
    public JarIndex(String mainJarPath, InputStream jarIndex, List<String> classpath) {
        this.mainJarPath = mainJarPath;
        this.classpath = classpath;
        this.jarIndex = new ConcurrentSkipListMap<>();
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
    public URL[] getJarURLs() {
        return this.urls;
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
     * 动态添加 jar index，为动态添加 class 提供支持
     *
     * @param jarFiles jar 文件集合
     */
    @SneakyThrows(IOException.class)
    public void addJarIndex(List<JarFile> jarFiles) {
        Map<String, Set<String>> indexContainer = new HashMap<>(jarFiles.size());
        for (JarFile jarFile : jarFiles) {
            try (JarFile jar = jarFile) {
                BuildJarIndexAntTask.scanJarIndex(jar.getName(), jar, indexContainer);
            }
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
        this.rebuildURLs();
    }

    /**
     * 动态移除 jar index
     *
     * @param jarFiles jar 文件
     */
    public void removeJarIndex(List<JarFile> jarFiles) {
        Map<String, Set<String>> indexContainer = new HashMap<>(jarFiles.size());
        for (JarFile jarFile : jarFiles) {
            BuildJarIndexAntTask.scanJarIndex(jarFile.getName(), jarFile, indexContainer);
        }
        for (Map.Entry<String, Set<String>> entry : indexContainer.entrySet()) {
            for (String _package_ : entry.getValue()) {
                List<String> jars = this.jarIndex.get(_package_);
                if (jars != null && !jars.isEmpty()) {
                    if (!jars.remove(entry.getKey())) {
                        System.err.println("remove jar index failed, please check jar file name.");
                    }
                    if (jars.isEmpty()) {
                        this.jarIndex.remove(_package_);
                    }
                }
            }
        }
        this.rebuildURLs();
    }

    /**
     * 动态移除 jar index
     *
     * @param packageName 包名，该包名下的所有 jar 都将被移除
     */
    public void removeJarIndex(String packageName) {
        this.jarIndex.remove(packageName);
        this.rebuildURLs();
    }

    /**
     * 根据资源获取所在 jar 包集合
     *
     * @param name 资源名称，可能根据该名称获取到包名
     * @return jars
     */
    public List<String> getJarFiles(String name) {
        int lastDot = name.lastIndexOf(name.endsWith(".class") ? '/' : '.');                                            // 为 com/kfyty/demo/Demo.class 提供支持
        if (lastDot < 0) {
            if (name.charAt(name.length() - 1) == '/') {
                return this.jarIndex.getOrDefault(name.substring(0, name.length() - 1), Collections.emptyList());       // 为 com/kfyty/demo/ 提供支持
            }
            return this.jarIndex.getOrDefault(name, Collections.emptyList());
        }
        String path = name.substring(0, lastDot);
        return this.getJarFiles(name, path);
    }

    /**
     * 根据资源获取所在 jar 包集合
     *
     * @param name        资源名称
     * @param packageName 包名称
     * @return jars
     */
    public List<String> getJarFiles(String name, String packageName) {
        List<String> jarFiles = this.jarIndex.get(packageName);
        return jarFiles != null ? jarFiles : this.jarIndex.getOrDefault(name, Collections.emptyList());
    }

    /**
     * 根据 JarFile 构建一个 URL
     *
     * @param jarFile Jar file
     * @return jar file url
     */
    public static URL getJarURL(JarFile jarFile) {
        return getJarURL(jarFile.getName());
    }

    /**
     * 根据 JarFile 构建一个 URL
     *
     * @param jarFilePath Jar file path
     * @return jar file url
     */
    @SneakyThrows(MalformedURLException.class)
    public static URL getJarURL(String jarFilePath) {
        String path = jarFilePath.charAt(0) == '/' ? jarFilePath : '/' + jarFilePath;
        if (jarFilePath.endsWith(".jar")) {
            return new URL("file", "", -1, path.replace(File.separatorChar, '/'));             // 必须使用 file 协议，否则读取不到 resources
        }
        String filePath = path.charAt(path.length() - 1) == File.separatorChar ? path : path + File.separatorChar;
        return new URL("file", "", -1, filePath.replace(File.separatorChar, '/'));             // 必须转换为 '/'，否则 toURI 语法错误
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
        boolean mainJarResolved = this.isExploded() || !this.jarIndex.isEmpty();
        String parentPath = Paths.get(mainJarPath).getParent().toString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jarIndex))) {
            while ((line = reader.readLine()) != null) {
                if (mainJarResolved) {
                    this.loadJarIndex(parentPath, line, reader);
                } else {
                    if (line.equals(BuildJarIndexAntTask.MAIN_JAR_NAME)) {
                        line = mainJarPath;
                        mainJarResolved = true;
                    }
                    this.loadJarIndex(parentPath, line, reader);
                }
            }
        }
        this.rebuildURLs();
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
            this.jarIndex.computeIfAbsent(currentLine.intern(), k -> new LinkedList<>()).add(jarFile.intern());
        }
    }

    protected void rebuildURLs() {
        List<URL> urls = this.jarIndex.values().stream().flatMap(Collection::stream).distinct().map(JarIndex::getJarURL).collect(Collectors.toList());
        if (this.isExploded()) {
            this.classpath.stream().filter(e -> !e.endsWith(".jar")).map(JarIndex::getJarURL).forEach(urls::add);
        }
        this.urls = urls.toArray(new URL[0]);
    }
}
