package com.kfyty.javafx.core;

import com.kfyty.core.utils.ExceptionUtil;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * 描述: 生命周期支持的控制器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public interface LifeCycleController extends LifeCycleBinder {
    /**
     * @see Window#onShowing
     */
    default void onShowing(WindowEvent event) {

    }

    /**
     * @see Window#onShown
     */
    default void onShown(WindowEvent event) {

    }

    /**
     * @see Window#onHiding
     */
    default void onHiding(WindowEvent event) {

    }

    /**
     * @see Window#onHidden
     */
    default void onHidden(WindowEvent event) {

    }

    /**
     * 当修改模型数据，绑定到视图时出现异常回调
     *
     * @param view      要绑定的视图
     * @param value     要绑定的值
     * @param throwable 绑定异常
     */
    default void onViewBindCause(ObservableValue<?> view, Object value, Throwable throwable) {
        throw ExceptionUtil.wrap(throwable);
    }

    /**
     * 当修改视图数据，绑定到模型时出现异常回调
     *
     * @param target    触发绑定事件目标
     * @param bindPath  值绑定路径
     * @param value     视图值
     * @param throwable 绑定异常
     */
    default void onModelBindCause(ObservableValue<?> target, String bindPath, Object value, Throwable throwable) {
        throw ExceptionUtil.wrap(throwable);
    }

    /**
     * 子窗口关闭时调用
     *
     * @param name       子窗口名称
     * @param child      子窗口
     * @param controller 子窗口控制器
     */
    default void onChildClose(String name, Node child, AbstractController controller) {

    }

    /**
     * @see Window#onCloseRequest
     */
    default void onClose(WindowEvent event) {

    }
}
