package com.kfyty.loveqq.framework.javafx.core;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.javafx.core.annotation.FController;
import com.kfyty.loveqq.framework.javafx.core.event.PrimaryStageLoadedEvent;
import com.kfyty.loveqq.framework.javafx.core.factory.FXMLComponentFactoryBean;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * 描述: 引导应用
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class BootstrapApplication extends AbstractApplication implements CommandLineRunner, ApplicationContextAware {
    /**
     * ioc 容器上下文
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * {@link HostServices}
     */
    private static HostServices hostServices;

    /**
     * javafx 应用是否已启动
     */
    private final CountDownLatch startedLatch = new CountDownLatch(1);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        BootstrapApplication.applicationContext = applicationContext;
    }

    @Override
    public void start(Stage primaryStage) {
        BootstrapApplication.hostServices = super.getHostServices();
        Collection<BeanDefinition> beanDefinitions = applicationContext.getBeanDefinitionWithAnnotation(FController.class, true).values();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            FController annotation = AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), FController.class);
            if (annotation != null && annotation.main()) {
                Scene root = BootstrapApplication.getBean(annotation.value());
                FXMLComponentFactoryBean.initWindowProperties(primaryStage, annotation);
                BootstrapApplication.publishEvent(new PrimaryStageLoadedEvent(this.bindLifeCycle(primaryStage, root)));
                primaryStage.show();
                break;
            }
        }
        this.startedLatch.countDown();
    }

    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> Application.launch(this.getClass(), args)).start();
        this.startedLatch.await();
    }

    public static HostServices getHostService() {
        return hostServices;
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
