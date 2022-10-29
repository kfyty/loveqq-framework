package com.kfyty.boot;

import com.kfyty.boot.context.factory.ApplicationContextFactory;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.CommandLineRunner;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 功能描述: 启动类
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:46
 * @since JDK 1.8
 */
@Data
@Slf4j
@NoArgsConstructor
public class K {
    private Class<?> primarySource;
    private String[] commandLineArgs;
    private ApplicationContextFactory applicationContextFactory;

    public K(Class<?> clazz, String... args) {
        this.primarySource = clazz;
        this.commandLineArgs = args;
        this.setApplicationContextFactory(new ApplicationContextFactory());
    }

    public void setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
        this.applicationContextFactory = applicationContextFactory;
    }

    public ApplicationContext run() {
        log.info("Boot loading...");
        long start = System.currentTimeMillis();
        ApplicationContext applicationContext = this.applicationContextFactory.create(this).refresh();
        log.info("Started {} in {} seconds", applicationContext.getPrimarySource().getSimpleName(), (System.currentTimeMillis() - start) / 1000D);
        this.invokeRunner(applicationContext);
        return applicationContext;
    }

    public static ApplicationContext run(Class<?> clazz, String... args) {
        return new K(clazz, args).run();
    }

    protected void invokeRunner(ApplicationContext applicationContext) {
        for (CommandLineRunner commandLineRunner : applicationContext.getBeanOfType(CommandLineRunner.class).values()) {
            try {
                commandLineRunner.run(this.commandLineArgs);
            } catch (Exception ex) {
                throw new IllegalStateException("failed to execute CommandLineRunner", ex);
            }
        }
    }
}
