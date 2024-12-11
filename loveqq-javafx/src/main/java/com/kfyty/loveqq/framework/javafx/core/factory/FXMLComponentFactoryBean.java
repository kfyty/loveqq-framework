package com.kfyty.loveqq.framework.javafx.core.factory;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.beans.FactoryBean;
import com.kfyty.loveqq.framework.core.exception.ResolvableException;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.javafx.core.AbstractController;
import com.kfyty.loveqq.framework.javafx.core.LifeCycleController;
import com.kfyty.loveqq.framework.javafx.core.annotation.FController;
import com.kfyty.loveqq.framework.javafx.core.event.FEventListenerAdapter;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * 描述: 加载 fxml 组件
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class FXMLComponentFactoryBean implements FactoryBean<Object> {
    /**
     * 控制器 bean name
     */
    private final String controllerBeanName;

    /**
     * fxml 路径
     */
    private final String path;

    /**
     * 控制器注解
     */
    private final FController fController;

    @Autowired
    private ControllerFactory controllerFactory;

    @Autowired(FEventListenerAdapter.BEAN_NAME)
    private FEventListenerAdapter eventListenerAdapter;

    @Override
    public Class<?> getBeanType() {
        return this.fController.main() ? Scene.class : this.fController.window() ? Stage.class : this.fController.componentType();
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Object getObject() {
        try {
            // 加载 fxml 文件
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(this.getClass().getResource(this.path));
            fxmlLoader.setControllerFactory(this.controllerFactory);
            Parent component = fxmlLoader.load();

            // 绑定视图和控制器的对应关系
            component.getProperties().put(component, fxmlLoader);
            if (fxmlLoader.getController() instanceof AbstractController controller) {
                controller.setView(component);
            }

            // 添加到事件监听器缓存
            this.eventListenerAdapter.addController(this.controllerBeanName, component, fxmlLoader.getController());

            // 包装组件
            if (this.getBeanType() == Scene.class) {
                return this.createScene(component);
            }

            if (this.getBeanType() != Stage.class) {
                return component;
            }

            Stage window = new Stage();
            initWindowProperties(window, this.fController);

            // 绑定生命周期
            if (fxmlLoader.getController() instanceof LifeCycleController controller) {
                controller.bindLifeCycle(window, this.createScene(component));
            } else {
                window.setScene(this.createScene(component));
            }

            if (fxmlLoader.getController() instanceof AbstractController controller) {
                controller.setWindow(window);
            }

            if (this.fController.show()) {
                window.show();
            }

            return window;
        } catch (IOException e) {
            throw new ResolvableException("failed load FXML component: " + this.path, e);
        }
    }

    protected Scene createScene(Parent component) {
        Scene scene = new Scene(component);
        if (CommonUtil.notEmpty(this.fController.css())) {
            scene.getStylesheets().addAll(this.fController.css());
        }
        return scene;
    }

    public static void initWindowProperties(Stage window, FController fController) {
        window.initStyle(fController.stageStyle());
        window.setTitle(fController.title());
        window.setFullScreen(fController.fullScreen());
        window.setResizable(fController.resizable());
        window.setAlwaysOnTop(fController.alwaysOnTop());
        if (CommonUtil.notEmpty(fController.icon())) {
            window.getIcons().clear();
            window.getIcons().add(new Image(fController.icon()));
        }
    }
}
