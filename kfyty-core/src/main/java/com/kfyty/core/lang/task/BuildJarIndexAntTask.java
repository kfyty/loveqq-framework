package com.kfyty.core.lang.task;

import com.kfyty.core.lang.JarIndex;
import com.kfyty.core.support.BootLauncher;
import com.kfyty.core.support.EnumerationIterator;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.IOUtil;

import java.io.File;
import java.io.IOException;
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

import static com.kfyty.core.utils.CommonUtil.loadCommandLineProperties;
import static com.kfyty.core.utils.IOUtil.newInputStream;

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
     * 由 maven-antrun-plugin 调用
     *
     * @param args 由 maven-antrun-plugin 传参
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> properties = loadCommandLineProperties(args, "-");
        File file = new File(properties.get(OUTPUT_DIRECTORY), properties.getOrDefault(OUTPUT_JAR, properties.get(OUTPUT_DEFAULT_JAR)));
        JarFile jarFile = new JarFile(file.exists() ? file : new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_DEFAULT_JAR)));
        writeJarIndex(buildJarIndex(scanJarIndex(jarFile)), jarFile);
        System.out.println("[INFO] Build jar index succeed");
    }

    /**
     * 扫描依赖并生成 jar index 数据结构
     * 要求 jar 文件中包含 Class-Path 描述
     *
     * @param targetJar 已生成的 jar 文件
     * @return jar index  key: jar class path, value: package
     */
    public static Map<String, Set<String>> scanJarIndex(JarFile targetJar) throws Exception {
        Map<String, Set<String>> index = new HashMap<>();
        String parentPath = Paths.get(targetJar.getName()).getParent().toString();
        List<String> classPath = CommonUtil.split(targetJar.getManifest().getMainAttributes().getValue("Class-Path"), " ");
        for (String jarPath : classPath) {
            try (JarFile jarFile = new JarFile(new File(parentPath, jarPath))) {
                scanJarIndex(jarPath, jarFile, index);
            }
        }
        scanJarIndex(new File(targetJar.getName()).getName(), targetJar, index);
        return index;
    }

    public static void scanJarIndex(String jarPath, JarFile jarFile, Map<String, Set<String>> indexContainer) {
        for (JarEntry entry : new EnumerationIterator<>(jarFile.entries())) {
            String entryName = entry.getName();

            // 非 class 文件，仅保留 META-INF 资源索引
            if (!entryName.endsWith(".class")) {
                if (entryName.length() > 9 && entryName.startsWith("META-INF/") && !entryName.startsWith("META-INF/maven")) {
                    indexContainer.computeIfAbsent(jarPath, k -> new HashSet<>()).add(entryName);
                }
                continue;
            }

            // class 文件索引
            int packageIndex = entryName.lastIndexOf('/');
            if (packageIndex > 0) {
                String packageName = entryName.substring(0, packageIndex);
                indexContainer.computeIfAbsent(jarPath, k -> new HashSet<>()).add(packageName);
            }
        }
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
     * 写入 jar.idx 文件
     *
     * @param jarIndex jar.idx
     * @param jarFile  目标 jar 文件
     */
    public static void writeJarIndex(String jarIndex, JarFile jarFile) throws IOException {
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
        }
    }
}
