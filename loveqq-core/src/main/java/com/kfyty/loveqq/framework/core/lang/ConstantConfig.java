package com.kfyty.loveqq.framework.core.lang;

import java.lang.instrument.ClassFileTransformer;
import java.util.Optional;

/**
 * 描述: 系统常量配置
 *
 * @author kfyty725
 * @date 2024/9/2 19:05
 * @email kfyty725@hotmail.com
 */
public interface ConstantConfig {
    /**
     * 是否读取 {@link ClassFileTransformer}
     * 开启此配置，会使用 javassist 修改字节码，会少量内存使用量
     */
    boolean LOAD_TRANSFORMER = Boolean.parseBoolean(System.getProperty("k.transformer.load", "true"));

    /**
     * 是否读取进行依赖检查，出现依赖冲突导致启动失败时，可打开
     */
    boolean DEPENDENCY_CHECK = Boolean.parseBoolean(System.getProperty("k.dependency.check", "false"));

    /**
     * java 内部资源不在 jar index 内，{@link JarIndexClassLoader#getResource(String)} 可能获取不到，可通过该参数设置
     */
    String[] JAVA_SYSTEM_RESOURCES = Optional.ofNullable(System.getProperty("k.java.system.resources")).map(e -> e.split(";")).orElse(new String[0]);

    /**
     * {@link com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil#resolveClassPath(ClassLoader)} 时，是否读取 jar 内的 Class-Path 属性
     */
    boolean LOAD_JAR_CLASS_PATH = Boolean.parseBoolean(System.getProperty("k.dependency.load-jar-class-path", "false"));

    /**
     * 临时文件夹位置
     */
    String TEMP_PATH = System.getProperty("java.io.tmpdir");

    /**
     * 日志 root 级别
     */
    String LOGGING_ROOT_LEVEL = System.getProperty("logging.root", "INFO");

    /**
     * ioc 容器是否并行初始化 bean
     */
    boolean CONCURRENT_INITIALIZE = Boolean.parseBoolean(System.getProperty("k.concurrent-initialize", "false"));

    /**
     * 嵌套注解解析深度
     */
    int ANNOTATION_RESOLVE_DEPTH = Integer.parseInt(System.getProperty("k.annotation.depth", "99"));
}
