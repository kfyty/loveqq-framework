package com.kfyty.javafx.core;

import com.kfyty.core.proxy.MethodInterceptorChain;
import com.kfyty.core.proxy.MethodInterceptorChainPoint;
import com.kfyty.core.proxy.factory.DynamicProxyFactory;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.javafx.core.annotation.FView;
import com.kfyty.javafx.core.proxy.ViewModelBindProxy;
import com.kfyty.javafx.core.utils.ViewModelBindUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.kfyty.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.core.utils.AnnotationUtil.flatRepeatableAnnotation;

/**
 * 描述: 具有双向绑定能力的控制器，初始化时处理双向绑定
 * 必须在 {@link Initializable#initialize(URL, ResourceBundle)} 方法执行
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public interface ViewBindCapableController extends LifeCycleBinder, Initializable {

    @Override
    default void initialize(URL location, ResourceBundle resources) {
        this.initViewBind();
    }

    private void initViewBind() {
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(AopUtil.getTargetClass(this));
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            Annotation[] annotations = Arrays.stream(flatRepeatableAnnotation(findAnnotations(entry.getValue()))).filter(e -> e.annotationType().equals(FView.class)).toArray(Annotation[]::new);
            for (Annotation view : annotations) {
                this.initViewBind(entry.getValue(), (FView) view);
            }
        }
    }

    private void initViewBind(Field field, FView view) {
        if (CommonUtil.empty(view.value())) {
            return;
        }

        String[] split = view.value().split(":");
        String property = split[0];
        String bindPath = split[1];
        Node viewNode = (Node) ReflectUtil.getFieldValue(this, field);

        this.initModelBind(property, bindPath, viewNode, view);
        this.initViewBind(property, bindPath, viewNode, view);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void initModelBind(String property, String bindPath, Node viewNode, FView view) {
        // 获取绑定的属性
        boolean hasInitValue = true;
        Field modelProp = ReflectUtil.getField(this.getClass(), bindPath.split("\\.")[0]);
        ObservableValue<?> propertyValue = ViewModelBindUtil.resolveView(property, viewNode, view);
        Object modelValue = ReflectUtil.getFieldValue(this, modelProp);

        // 空值时初始化一个默认值
        if (modelValue == null) {
            hasInitValue = false;
            modelValue = ReflectUtil.newInstance(modelProp.getType());
        }

        // 添加模型绑定代理
        // 还不是代理，创建代理
        if (!AopUtil.isProxy(modelValue)) {
            modelValue = DynamicProxyFactory.create(true).addInterceptorPoint(new ViewModelBindProxy(this, new LinkedList<>())).createProxy(modelValue);
        }
        // 已经是代理，但可能不是上面创建的代理，添加拦截点
        MethodInterceptorChain chain = AopUtil.getProxyInterceptorChain(modelValue);
        Optional<MethodInterceptorChainPoint> anyChainPoint = chain.getChainPoints().stream().filter(e -> e instanceof ViewModelBindProxy).findAny();
        if (anyChainPoint.isEmpty()) {
            chain.addInterceptorPoint(new ViewModelBindProxy(this, new LinkedList<>()));
        }
        ViewModelBindProxy proxy = (ViewModelBindProxy) chain.getChainPoints().stream().filter(e -> e instanceof ViewModelBindProxy).findAny().get();
        proxy.addBindView(bindPath, propertyValue);

        // 更新视图默认值
        if (hasInitValue) {
            Object defaultValue = ReflectUtil.parseValue(bindPath, this);
            if (defaultValue != null) {
                proxy.viewBind(propertyValue, defaultValue);
            }
        }

        // 更新属性为代理
        ReflectUtil.setFieldValue(this, modelProp, modelValue);
    }

    private void initViewBind(String property, String bindPath, Node viewNode, FView view) {
        ObservableValue<?> propertyValue = ViewModelBindUtil.resolveView(property, viewNode, view);
        Object modelValue = ReflectUtil.getFieldValue(this, bindPath.split("\\.")[0]);

        ViewBindEventHandler viewBindEventHandler = new ViewBindEventHandler(bindPath.substring(bindPath.indexOf('.') + 1), AopUtil.getTarget(modelValue), this);
        propertyValue.addListener(viewBindEventHandler);

        if (propertyValue instanceof ObjectProperty<?> objectProperty) {
            Object value = objectProperty.getValue();
            if (value instanceof ObservableList<?> observableList) {
                observableList.addListener(new ListChangeListener<Object>() {
                    @Override
                    public void onChanged(Change<?> c) {
                        viewBindEventHandler.changed(propertyValue, c.getList(), c.getList());
                    }
                });
            }
            if (value instanceof ObservableSet<?> observableSet) {
                observableSet.addListener(new SetChangeListener<Object>() {
                    @Override
                    public void onChanged(Change<?> c) {
                        viewBindEventHandler.changed(propertyValue, c.getSet(), c.getSet());
                    }
                });
            }
            if (value instanceof ObservableMap<?, ?> observableMap) {
                observableMap.addListener(new MapChangeListener<Object, Object>() {
                    @Override
                    public void onChanged(Change<?, ?> c) {
                        viewBindEventHandler.changed(propertyValue, c.getMap(), c.getMap());
                    }
                });
            }
        }
    }

    @RequiredArgsConstructor
    class ViewBindEventHandler implements ChangeListener<Object> {
        private final String bindPath;
        private final Object model;
        private final ViewBindCapableController controller;

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            try {
                if (newValue != null) {
                    ReflectUtil.setNestedFieldValue(this.bindPath, this.model, newValue);
                }
            } catch (Throwable e) {
                if (this.controller instanceof LifeCycleController lifeCycleController) {
                    lifeCycleController.onModelBindCause(observable, this.bindPath, newValue, e);
                    return;
                }
                throw e;
            }
        }
    }
}
