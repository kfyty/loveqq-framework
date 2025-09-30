package com.kfyty.loveqq.framework.boot;

import com.kfyty.loveqq.framework.boot.context.factory.ApplicationContextFactory;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.lang.JarIndexClassLoader;
import com.kfyty.loveqq.framework.core.utils.ClassLoaderUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能描述: 启动类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:46
 * @since JDK 1.8
 */
@Data
@Builder
@AllArgsConstructor
public class K {
    /**
     * 启动类
     */
    private Class<?> primarySource;

    /**
     * 命令行参数
     */
    private String[] commandLineArgs;

    /**
     * {@link ApplicationContextFactory}
     */
    private ApplicationContextFactory applicationContextFactory;

    /**
     * 构造器
     *
     * @param clazz 启动类
     * @param args  命令行参数
     */
    public K(Class<?> clazz, String... args) {
        this(clazz, args, new ApplicationContextFactory());
    }

    /**
     * 启动应用
     * 这里的 log 不是常亮是避免引导启动时加载过多的类
     * 事实上，该类所引用的类都应该避免使用 log，以避免引导启动时加载过多的类
     *
     * @return {@link ApplicationContext}
     */
    public ApplicationContext run() {
        Logger log = LoggerFactory.getLogger(this.primarySource);

        log.info("Boot loading...");

        long start = System.currentTimeMillis();

        ApplicationContext applicationContext = this.applicationContextFactory.create(this).refresh();

        log.info("Started {} in {} seconds", applicationContext.getPrimarySource().getSimpleName(), (System.currentTimeMillis() - start) / 1000D);

        this.invokeRunner(applicationContext);

        return applicationContext;
    }

    /**
     * 直接启动应用
     * 运行该方法时，如果类加载不是 {@link JarIndexClassLoader}，
     * 则 {@link java.lang.instrument.ClassFileTransformer} 不会直接应用，仍需配置到 javaagent 以及 Pre-Main 才能生效
     * <p>
     * 但是，当打包后运行时，将由 {@link com.kfyty.loveqq.framework.core.support.BootLauncher} 引导启动，一定会是 {@link JarIndexClassLoader}
     * 因此，打包后仍无需 javaagent 配置即可令 {@link java.lang.instrument.ClassFileTransformer} 生效
     *
     * @return {@link ApplicationContext}
     */
    public static ApplicationContext start(Class<?> clazz, String... args) {
        return new K(clazz, args).run();
    }

    /**
     * 启动应用，自动构建 {@link JarIndexClassLoader} 启动
     * {@link java.lang.instrument.ClassFileTransformer} 会直接应用，无需配置到 javaagent 以及 Pre-Main
     * 没有返回值是因为会出现 {@link ClassCastException}，原因是切换了类加载器
     *
     * @param clazz 启动类
     * @param args  命令行参数
     */
    public static void run(Class<?> clazz, String... args) {
        runOnClassLoader(ClassLoaderUtil.getIndexedClassloader(clazz), clazz, args);
    }

    /**
     * 使用指定的类加载器启动应用
     *
     * @param classLoader 类加载器
     * @param clazz       启动类
     * @param args        命令行参数
     */
    @SneakyThrows(Exception.class)
    public static void runOnClassLoader(ClassLoader classLoader, Class<?> clazz, String... args) {
        Class<?> bootClass = Class.forName(K.class.getName(), false, classLoader);
        Class<?> primaryClass = Class.forName(clazz.getName(), false, classLoader);
        Thread.currentThread().setContextClassLoader(classLoader);
        bootClass.getMethod("start", Class.class, String[].class).invoke(null, primaryClass, args);
    }

    /**
     * 应用启动完成后执行命令行运行器
     *
     * @param applicationContext 应用上下文
     */
    protected void invokeRunner(ApplicationContext applicationContext) {
        for (CommandLineRunner commandLineRunner : applicationContext.getBeanOfType(CommandLineRunner.class).values()) {
            try {
                commandLineRunner.run(this.commandLineArgs);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to execute CommandLineRunner", ex);
            }
        }
    }
}
