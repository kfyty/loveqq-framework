package com.kfyty.javafx.core;

import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.javafx.core.annotation.FView;
import com.kfyty.javafx.core.proxy.ViewModelBindProxy;
import com.kfyty.javafx.core.utils.ViewModelBindUtil;
import javafx.beans.property.ObjectProperty;
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
    /**
     * 初始化视图模型绑定
     * <p>
     * 自定义初始化请实现 {@link this#init()} 方法
     */
    @Override
    default void initialize(URL location, ResourceBundle resources) {
        this.initViewBind();
        this.init();
    }

    /**
     * 自定义初始化回调
     */
    default void init() {

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
        ObservableValue<?> propertyValue = ViewModelBindUtil.resolveView(property, viewNode, view);
        Pair<Field, Object> fieldValuePair = ViewModelBindUtil.resolveModel(this, bindPath, view);
        Field modelProp = fieldValuePair.getKey();
        Object modelValue = fieldValuePair.getValue();

        // 空值时初始化一个默认值
        if (modelValue == null) {
            modelValue = ReflectUtil.newInstance(modelProp.getType());
        }

        // 添加模型绑定代理
        ViewModelBindProxy proxy = ViewModelBindUtil.bindModelProxy(this, modelProp, modelValue);
        proxy.addBindView(bindPath, propertyValue);
    }

    private void initViewBind(String property, String bindPath, Node viewNode, FView view) {
        ObservableValue<?> propertyValue = ViewModelBindUtil.resolveView(property, viewNode, view);
        Object modelValue = ViewModelBindUtil.resolveModel(this, bindPath, view).getValue();

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
