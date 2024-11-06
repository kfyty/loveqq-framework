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
     * 嵌套注解解析深度
     */
    int ANNOTATION_RESOLVE_DEPTH = Integer.parseInt(System.getProperty("k.annotation.depth", "99"));

    /**
     * 临时文件夹位置
     */
    String TEMP_PATH = System.getProperty("java.io.tmpdir");

    /**
     * 日志 root 级别
     */
    String LOGGING_ROOT_LEVEL = System.getProperty("logging.root", "INFO");

    /**
     * 应用名称的配置 key
     */
    String APPLICATION_NAME_KEY = "k.application.name";

    /**
     * 应用是否整体懒加载，true 时，仅加载 bean 定义，所有 bean 都不会进行初始化
     */
    String LAZY_INIT_KEY = "k.application.lazy-init";

    /**
     * 应用是否并行初始化，true 时将使用线程池初始化所有的单例 bean
     */
    String CONCURRENT_INIT_KEY = "k.application.concurrent-init";

    /**
     * web 服务器端口的配置 key
     */
    String SERVER_PORT_KEY = "k.server.port";

    /**
     * 配置文件中引用对象配置 key，即: ${ref:key}
     */
    String REF_CONFIG_KEY = "ref";

    /**
     * 配置文件中包含其他配置的配置 key
     */
    String IMPORT_KEY = "k.config.include";

    /**
     * 配置文件中包含本地磁盘的配置 key
     * 通过该配置指定的配置文件，支持热刷新
     * 注意：只有通过该配置指定的才支持热刷新，内部通过 {@link #IMPORT_KEY} 包含的嵌套配置不支持
     */
    String LOCATION_KEY = "k.config.location";

    /**
     * 是否加载系统属性到属性配置的配置 key
     */
    String LOAD_SYSTEM_PROPERTY_KEY = "k.config.load-system-property";
}
