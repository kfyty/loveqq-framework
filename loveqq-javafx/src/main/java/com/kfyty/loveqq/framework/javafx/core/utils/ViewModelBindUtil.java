package com.kfyty.loveqq.framework.javafx.core.utils;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.NestedConfigurationProperty;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChain;
import com.kfyty.loveqq.framework.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.loveqq.framework.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.javafx.core.AbstractViewModelBindCapableController;
import com.kfyty.loveqq.framework.javafx.core.annotation.FView;
import com.kfyty.loveqq.framework.javafx.core.proxy.ViewModelBindProxy;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Optional;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.hasAnnotation;

/**
 * 描述: 视图模型绑定工具
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class ViewModelBindUtil {
    /**
     * 解析模型
     */
    public static Pair<Field, Object> resolveModel(Object controller, String bindPath, FView view) {
        String property = bindPath.split("\\.")[0];
        Field field = ReflectUtil.getField(controller.getClass(), property);
        if (field == null) {
            throw new IllegalArgumentException("bind model failed, field doesn't exists: " + property);
        }
        return new Pair<>(field, ReflectUtil.getFieldValue(controller, field));
    }

    /**
     * 解析视图
     */
    public static ObservableValue<?> resolveView(String property, Node viewNode, FView view) {
        if (!view.method()) {
            Field nodeProperty = ReflectUtil.getField(viewNode.getClass(), property);
            if (nodeProperty == null) {
                throw new IllegalArgumentException("bind view failed, view doesn't exists: " + property);
            }
            return (ObservableValue<?>) ReflectUtil.getFieldValue(viewNode, nodeProperty, false);
        }
        Object value = viewNode;
        String[] split = property.split("\\.");
        for (String methodName : split) {
            Method method = ReflectUtil.getMethod(value.getClass(), methodName);
            if (method == null) {
                throw new IllegalArgumentException("bind view failed, view method doesn't exists: " + property);
            }
            value = ReflectUtil.invokeMethod(value, method);
        }
        return (ObservableValue<?>) value;
    }

    public static ViewModelBindProxy bindModelProxy(AbstractViewModelBindCapableController controller, Field modelProp, Object modelValue) {
        return bindModelProxy(controller, modelProp, modelValue, new ViewModelBindProxy(controller, new LinkedList<>()));
    }

    public static ViewModelBindProxy bindModelProxy(Object root, Field modelProp, Object modelValue, ViewModelBindProxy proxy) {
        // 还不是代理，创建代理
        if (!AopUtil.isProxy(modelValue)) {
            modelValue = DynamicProxyFactory.create(true).addInterceptorPoint(proxy).createProxy(modelValue);
        }

        // 已经是代理，但可能不是上面创建的代理，添加拦截点
        MethodInterceptorChain chain = AopUtil.getProxyInterceptorChain(modelValue);
        Optional<MethodInterceptorChainPoint> anyChainPoint = chain.getChainPoints().stream().filter(e -> e instanceof ViewModelBindProxy).findAny();
        if (anyChainPoint.isEmpty()) {
            chain.addInterceptorPoint(proxy);
        } else {
            proxy = (ViewModelBindProxy) anyChainPoint.get();
        }

        // 更新属性为代理
        ReflectUtil.setFieldValue(root, modelProp, modelValue);

        // 为嵌套的属性添加相同的代理
        bindNestedModelProxy(chain.getTarget(), proxy);

        return proxy;
    }

    public static void bindNestedModelProxy(Object root, ViewModelBindProxy proxy) {
        for (Field field : ReflectUtil.getFieldMap(root.getClass()).values()) {
            if (hasAnnotation(field, NestedConfigurationProperty.class) || hasAnnotation(field.getType(), NestedConfigurationProperty.class)) {
                Object child = ReflectUtil.getFieldValue(root, field);
                if (child == null) {
                    child = ReflectUtil.newInstance(field.getType());
                }
                bindModelProxy(root, field, child, proxy);
            }
        }
    }
}
