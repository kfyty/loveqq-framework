package com.kfyty.javafx.core.binder;

import javafx.beans.value.WritableValue;

/**
 * 描述: 视图属性绑定器，用于将模式数据设置到视图中
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public interface ViewPropertyBinder {
    /**
     * 是否支持给定的类型
     *
     * @param view   视图
     * @param target 目标属性类型
     * @return true if support
     */
    boolean support(WritableValue<?> view, Class<?> target);

    /**
     * 绑定视图值
     *
     * @param view  视图
     * @param model 绑定值
     */
    void bind(WritableValue<?> view, Object model);
}
