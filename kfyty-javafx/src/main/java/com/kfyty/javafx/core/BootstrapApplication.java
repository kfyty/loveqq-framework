package com.kfyty.javafx.core;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.CommandLineRunner;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.event.ApplicationEvent;
import javafx.application.Application;
import lombok.Getter;

/**
 * 描述: 引导应用
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class BootstrapApplication implements CommandLineRunner, ApplicationContextAware {
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        BootstrapApplication.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) throws Exception {
        Application application = BootstrapApplication.applicationContext.getBean(Application.class);
        Application.launch(application.getClass(), args);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static void publishEvent(ApplicationEvent<?> event) {
        applicationContext.publishEvent(event);
    }
}
