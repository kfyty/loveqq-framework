package com.kfyty.loveqq.framework.core.lang.task;

import com.kfyty.loveqq.framework.core.lang.JarIndex;
import com.kfyty.loveqq.framework.core.support.BootLauncher;
import com.kfyty.loveqq.framework.core.support.EnumerationIterator;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOUtil;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.loadCommandLineProperties;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.newInputStream;
import static com.kfyty.loveqq.framework.core.utils.ReflectUtil.load;

/**
 * 描述: 构建 jar 索引 ant 任务
 *
 * @author kfyty725
 * @date 2023/5/22 9:11
 * @email kfyty725@hotmail.com
 */
public class BuildJarIndexAntTask {
    /**
     * 项目输出目录
     */
    public static final String OUTPUT_DIRECTORY = "OUTPUT_DIRECTORY";

    /**
     * 项目最终构建的 jar 文件名称
     */
    public static final String OUTPUT_JAR = "OUTPUT_JAR";

    /**
     * 项目默认构建的 jar 文件名称
     */
    public static final String OUTPUT_DEFAULT_JAR = "OUTPUT_DEFAULT_JAR";

    /**
     * 源码 MANIFEST.MF 目录
     */
    public static final String PROJECT_SOURCE_CODE_MANIFEST_DIRECTORY = "src/main/resources/" + BootLauncher.JAR_MANIFEST_LOCATION;

    /**
     * 源码 jar.idx 目录
     */
    public static final String PROJECT_SOURCE_CODE_JAR_INDEX_DIRECTORY = "src/main/resources/" + BootLauncher.JAR_INDEX_LOCATION;

    /**
     * 由 maven-antrun-plugin 调用
     *
     * @param args 由 maven-antrun-plugin 传参
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> properties = loadCommandLineProperties(args, "-");
        File file = new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_JAR));
        if (!file.exists()) {
            file = new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_DEFAULT_JAR));
            if (!file.exists()) {
                file = new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_DEFAULT_JAR).replace(".jar", "-SNAPSHOT.jar"));
            }
            if (!file.exists()) {
                throw new IllegalArgumentException("The OUTPUT_JAR parameter error, please set project.build.finalName and rebuild.");
            }
        }
        JarFile jarFile = new JarFile(file);
        String jarIndex = buildJarIndex(scanJarIndex(jarFile));
        JarFile sourceJarFile = writeJarIndex(jarIndex, jarFile);
        writeJarIndexToProjectSourceCode(jarIndex, sourceJarFile);
        System.out.println("[INFO] Build jar index succeed");
    }

    /**
     * 扫描依赖并生成 jar index 数据结构
     * 要求 jar 文件中包含 Class-Path 描述
     *
     * @param mainJar 启动类所在 jar 文件
     * @return jar index  key: jar class path, value: package
     */
    public static Map<String, Set<String>> scanJarIndex(JarFile mainJar) throws Exception {
        Map<String, Set<String>> index = new HashMap<>();
        String parentPath = Paths.get(mainJar.getName()).getParent().toString();
        List<String> classPath = CommonUtil.split(mainJar.getManifest().getMainAttributes().getValue("Class-Path"), " ");
        return scanJarIndex(mainJar, parentPath, classPath, index);
    }

    /**
     * 扫描依赖并生成 jar index 数据结构
     *
     * @param absoluteClassPath 绝对路径的 jars
     * @param indexContainer    jar index 容器
     * @return jar index  key: jar class path, value: package
     */
    public static Map<String, Set<String>> scanJarIndex(List<String> absoluteClassPath, Map<String, Set<String>> indexContainer) throws Exception {
        return scanJarIndex(null, "", absoluteClassPath, indexContainer);
    }

    /**
     * 扫描依赖并生成 jar index 数据结构
     *
     * @param mainJar           启动类所在 jar 文件
     * @param parentPath        jars 父路径
     * @param classRelativePath jar 相对路径
     * @param indexContainer    jar index 容器
     * @return jar index  key: jar class path, value: package
     */
    public static Map<String, Set<String>> scanJarIndex(JarFile mainJar, String parentPath, List<String> classRelativePath, Map<String, Set<String>> indexContainer) throws Exception {
        for (String relativePath : classRelativePath) {
            if (relativePath.endsWith(".jar")) {
                try (JarFile jarFile = new JarFile(new File(parentPath, relativePath.replace("%20", " ")))) {
                    scanJarIndex(relativePath, jarFile, indexContainer);
                }
            }
        }
        if (mainJar == null) {
            return indexContainer;
        }
        return scanJarIndex(new File(mainJar.getName()).getName(), mainJar, indexContainer);
    }

    /**
     * 扫描依赖并生成 jar index 数据结构
     *
     * @param relativePath   jar 相对路径
     * @param jarFile        jar 物理文件
     * @param indexContainer jar index 容器
     * @return jar index  key: jar class path, value: package
     */
    public static Map<String, Set<String>> scanJarIndex(final String relativePath, JarFile jarFile, Map<String, Set<String>> indexContainer) {
        for (JarEntry entry : new EnumerationIterator<>(jarFile.entries())) {
            String entryName = entry.getName();

            // 非 class 文件，不保留 META-INF/maven 资源索引
            if (!entryName.endsWith(".class")) {
                if (!entryName.startsWith("META-INF/maven")) {
                    if (entryName.charAt(entryName.length() - 1) == '/') {
                        entryName = entryName.substring(0, entryName.length() - 1);
                    }
                    indexContainer.computeIfAbsent(relativePath, k -> new HashSet<>()).add(entryName);
                }
                continue;
            }

            // class 文件索引
            int packageIndex = entryName.lastIndexOf('/');
            if (packageIndex > 0) {
                String packageName = entryName.substring(0, packageIndex);
                indexContainer.computeIfAbsent(relativePath, k -> new HashSet<>()).add(packageName);
            }
        }
        return indexContainer;
    }

    /**
     * 构建 jar.idx 索引文件
     * 不适用 INDEX.LIST 文件名称，避免 Class-Path 属性失效
     *
     * @param index 索引数据
     * @return jar.idx
     */
    public static String buildJarIndex(Map<String, Set<String>> index) {
        StringBuilder indexList = new StringBuilder();
        for (Map.Entry<String, Set<String>> entry : index.entrySet()) {
            String jar = entry.getKey();
            Set<String> jarIndex = entry.getValue();
            if (jarIndex.isEmpty()) {
                continue;
            }
            indexList.append(jar).append("\r\n");
            jarIndex.forEach(e -> indexList.append(e).append("\r\n"));
            indexList.append("\r\n");
        }
        return indexList.toString();
    }

    /**
     * 写入 jar.idx 文件到 jar
     *
     * @param jarIndex jar.idx
     * @param jarFile  目标 jar 文件
     * @return 原始 jar file
     */
    public static JarFile writeJarIndex(String jarIndex, JarFile jarFile) throws IOException {
        // 同时读取和写入，复制一份
        JarFile copy = new JarFile(IOUtil.writeToTemp(newInputStream(new File(jarFile.getName()))));
        try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(Paths.get(jarFile.getName())))) {
            for (JarEntry jarEntry : new EnumerationIterator<>(copy.entries())) {
                if (!jarEntry.getName().contains(JarIndex.JAR_INDEX_FILE_NAME)) {
                    jarOut.putNextEntry(jarEntry);
                    IOUtil.copy(copy.getInputStream(jarEntry), jarOut);
                    jarOut.closeEntry();
                }
            }
            jarOut.putNextEntry(new JarEntry(BootLauncher.JAR_INDEX_LOCATION));
            jarOut.write(jarIndex.getBytes(StandardCharsets.UTF_8));
            jarOut.closeEntry();
            jarOut.flush();
        }
        return copy;
    }

    /**
     * 写入 jar.idx 文件到工程源码
     * 从而支持 IDE 中从 {@link BootLauncher} 启动
     *
     * @param jarIndex jar.idx
     * @param jarFile  jar 文件
     */
    @SneakyThrows(URISyntaxException.class)
    public static void writeJarIndexToProjectSourceCode(String jarIndex, JarFile jarFile) throws IOException {
        String startClass = jarFile.getManifest().getMainAttributes().getValue(BootLauncher.START_CLASS_KEY);
        File projectSourceCodeDirectory = Paths.get(load(startClass).getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().getParent().toFile();

        // 写入 MANIFEST.MF
        try (InputStream manifest = jarFile.getInputStream(jarFile.getJarEntry(BootLauncher.JAR_MANIFEST_LOCATION));
             FileOutputStream out = new FileOutputStream(new File(projectSourceCodeDirectory, PROJECT_SOURCE_CODE_MANIFEST_DIRECTORY))) {
            IOUtil.copy(manifest, out);
        }

        // 写入 jar.idx
        try (FileOutputStream out = new FileOutputStream(new File(projectSourceCodeDirectory, PROJECT_SOURCE_CODE_JAR_INDEX_DIRECTORY))) {
            out.write(jarIndex.getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }
}
