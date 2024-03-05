package com.kfyty.javafx.core;

import com.kfyty.javafx.core.event.ViewCloseEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * 描述: 生命周期绑定器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public interface LifeCycleBinder {
    /**
     * 绑定组件以及生命周期
     *
     * @param window 窗口
     * @param root   组件
     * @return 窗口
     */
    default Stage bindLifeCycle(Stage window, Scene root) {
        window.setScene(root);
        return this.bindLifeCycle(window);
    }

    /**
     * 绑定生命周期
     *
     * @param window 窗口
     * @return 窗口
     */
    default Stage bindLifeCycle(Stage window) {
        Parent component = window.getScene().getRoot();
        Object controller = ((FXMLLoader) component.getProperties().get(component)).getController();
        if (controller instanceof LifeCycleController lifeCycleController) {
            window.setOnShowing(lifeCycleController::onShowing);
            window.setOnShown(lifeCycleController::onShown);
            window.setOnHiding(lifeCycleController::onHiding);
            window.setOnHidden(lifeCycleController::onHidden);
            window.setOnCloseRequest(event -> {
                lifeCycleController.onClose(event);
                BootstrapApplication.publishEvent(new ViewCloseEvent(component));
            });
        }
        return window;
    }

    /**
     * 注册子窗口
     * 该方法必须在父窗口控制器里调用
     * <p>
     * 下列方法在注册子窗口后才可用:
     * {@link LifeCycleController#onChildClose(String, Node, Object)}
     * {@link AbstractController#getChild(String)}
     * {@link AbstractController#getChildController(String)}
     *
     * @param child 子窗口
     */
    default Stage registerChild(Stage child) {
        Parent component = child.getScene().getRoot();
        Object controller = ((FXMLLoader) component.getProperties().get(component)).getController();
        if (this instanceof AbstractController<?> parent) {
            parent.addChild(component.getId(), component);
            if (controller instanceof AbstractController<?> childController) {
                childController.setParent(parent);
            }
        }
        EventHandler<WindowEvent> childEventHandler = child.getOnCloseRequest();
        child.setOnCloseRequest(event -> {
            childEventHandler.handle(event);
            if (this instanceof LifeCycleController parent && controller instanceof AbstractController<?> childController) {
                parent.onChildClose(component.getId(), component, childController);
            }
            if (this instanceof AbstractController<?> parent) {
                parent.removeChild(component.getId(), component);
            }
        });
        return child;
    }
}
