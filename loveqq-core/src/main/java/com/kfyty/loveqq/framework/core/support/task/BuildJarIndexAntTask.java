package com.kfyty.loveqq.framework.core.support.task;

import com.kfyty.loveqq.framework.core.lang.util.EnumerationIterator;
import com.kfyty.loveqq.framework.core.support.BootLauncher;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.kfyty.loveqq.framework.core.utils.CommonUtil.loadCommandLineProperties;
import static com.kfyty.loveqq.framework.core.utils.IOUtil.writeJarEntry;

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
     * 启动 jar 名称
     */
    public static final String MAIN_JAR_NAME = "__main__bootstrap__.jar";

    /**
     * 由 maven-antrun-plugin 调用
     *
     * @param args 由 maven-antrun-plugin 传参
     */
    public static void main(String[] args) throws Exception {
        Map<String, String> properties = loadCommandLineProperties(args, "-");
        JarFile jarFile = obtainJarFile(properties);
        String jarIndex = buildJarIndex(scanJarIndex(jarFile));
        writeJarEntry(BootLauncher.JAR_INDEX_LOCATION, jarIndex.getBytes(StandardCharsets.UTF_8), jarFile);
        System.out.println("[INFO] Build jar index succeed");
    }

    /**
     * 解析构建的 JarFile
     *
     * @param properties ant task 传递的命令行变量
     * @return JarFile
     */
    public static JarFile obtainJarFile(Map<String, String> properties) throws IOException {
        File file = new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_JAR));
        if (!file.exists()) {
            file = new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_DEFAULT_JAR));
            if (!file.exists()) {
                file = new File(properties.get(OUTPUT_DIRECTORY), properties.get(OUTPUT_DEFAULT_JAR).replace(".jar", "-SNAPSHOT.jar"));
            }
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("The " + OUTPUT_JAR + " parameter error, please set project.build.finalName and rebuild.");
        }
        return new JarFile(file);
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
        return scanJarIndex(null, null, absoluteClassPath, indexContainer);
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
                    if (mainJar != null) {
                        System.out.println("[INFO] Scanned jar index: " + jarFile.getName());
                    }
                }
            }
        }
        if (mainJar == null) {
            return indexContainer;
        }
        return scanJarIndex(MAIN_JAR_NAME, mainJar, indexContainer);
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
                        int length = entryName.length();
                        if (length == 1) {
                            continue;                                                                                   // '/' 忽略
                        }
                        entryName = entryName.substring(0, length - 1);                                                 // 这里会影响 JarIndex.getJarFiles()
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
}
