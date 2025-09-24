package com.kfyty.loveqq.framework.javafx.core;

import com.kfyty.loveqq.framework.core.autoconfig.CommandLineRunner;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.javafx.core.annotation.FController;
import com.kfyty.loveqq.framework.javafx.core.event.PrimaryStageLoadedEvent;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

/**
 * 描述: 引导应用
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class BootstrapApplication extends AbstractApplication implements CommandLineRunner {
    /**
     * {@link HostServices}
     */
    private static HostServices hostServices;

    /**
     * javafx 应用是否已启动
     */
    private final CountDownLatch startedLatch = new CountDownLatch(1);

    @Override
    public void start(Stage primaryStage) {
        BootstrapApplication.hostServices = super.getHostServices();
        Collection<BeanDefinition> beanDefinitions = IOC.getApplicationContext().getBeanDefinitionWithAnnotation(FController.class, true).values();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            FController annotation = AnnotationUtil.findAnnotation(beanDefinition.getBeanType(), FController.class);
            if (annotation != null && annotation.main()) {
                Stage root = IOC.getBean(annotation.value());
                IOC.publishEvent(new PrimaryStageLoadedEvent(primaryStage));
                Object controller = ((FXMLLoader) root.getScene().getRoot().getProperties().get(root.getScene().getRoot())).getController();
                if (controller instanceof AbstractController<?>) {
                    AbstractController<?> abstractController = (AbstractController<?>) controller;
                    abstractController.setInit(true);
                    abstractController.show();
                }
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
}
