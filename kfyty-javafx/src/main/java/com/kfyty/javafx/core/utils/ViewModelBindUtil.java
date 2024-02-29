package com.kfyty.javafx.core.utils;

import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.javafx.core.annotation.FView;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.lang.reflect.Field;

/**
 * 描述: 视图模型绑定工具
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class ViewModelBindUtil {
    /**
     * 解析视图
     */
    public static ObservableValue<?> resolveView(String property, Node viewNode, FView view) {
        if (!view.method()) {
            Field nodeProperty = ReflectUtil.getField(viewNode.getClass(), property);
            return (ObservableValue<?>) ReflectUtil.getFieldValue(viewNode, nodeProperty, false);
        }
        Object value = viewNode;
        String[] split = property.split("\\.");
        for (String methodName : split) {
            value = ReflectUtil.invokeMethod(value, methodName);
        }
        return (ObservableValue<?>) value;
    }
}
