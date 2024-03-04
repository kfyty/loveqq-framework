package com.kfyty.javafx.core;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.env.DataBinder;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.support.Instance;
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
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

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
public abstract class AbstractViewModelBindCapableController implements LifeCycleBinder, Initializable {
    /**
     * 参数key前缀
     */
    public static final String PARAMETER_PREFIX = "controller";

    /**
     * 数据绑定器
     */
    private DataBinder dataBinder;

    /**
     * 初始化视图模型绑定
     * <p>
     * 自定义初始化请实现 {@link this#init(URL, ResourceBundle)} 方法
     */
    @Override
    public final void initialize(URL location, ResourceBundle resources) {
        this.initViewBind();
        this.init(location, resources);
    }

    /**
     * 自定义初始化回调
     */
    public void init(URL location, ResourceBundle resources) {

    }

    /**
     * 获取数据绑定器
     *
     * @return {@link DataBinder}
     */
    public DataBinder getDataBinder() {
        return this.dataBinder.clone();
    }

    /**
     * 设置数据绑定器
     *
     * @param dataBinder {@link DataBinder}
     */
    @Autowired
    public void setDataBinder(DataBinder dataBinder) {
        this.dataBinder = dataBinder.clone();
    }

    protected void initViewBind() {
        DataBinder dataBinder = this.getDataBinder();
        Map<String, Field> fieldMap = ReflectUtil.getFieldMap(AopUtil.getTargetClass(this));
        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            Annotation[] annotations = Arrays.stream(flatRepeatableAnnotation(findAnnotations(entry.getValue()))).filter(e -> e.annotationType().equals(FView.class)).toArray(Annotation[]::new);
            for (Annotation view : annotations) {
                this.initViewBind(entry.getValue(), (FView) view, dataBinder);
            }
        }
    }

    protected void initViewBind(Field field, FView view, DataBinder dataBinder) {
        if (CommonUtil.empty(view.value())) {
            return;
        }

        String[] split = view.value().split(":");
        String property = split[0];
        String bindPath = split[1];
        Node viewNode = (Node) ReflectUtil.getFieldValue(this, field);

        this.initModelBind(property, bindPath, viewNode, view);
        this.initViewBind(property, bindPath, viewNode, view, dataBinder);
    }

    protected void initModelBind(String property, String bindPath, Node viewNode, FView view) {
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

    protected void initViewBind(String property, String bindPath, Node viewNode, FView view, DataBinder dataBinder) {
        ObservableValue<?> propertyValue = ViewModelBindUtil.resolveView(property, viewNode, view);
        ViewBindEventHandler viewBindEventHandler = new ViewBindEventHandler(bindPath, dataBinder, this);

        propertyValue.addListener(viewBindEventHandler);

        if (propertyValue instanceof ObjectProperty<?> objectProperty) {
            Object value = objectProperty.getValue();
            if (value instanceof ObservableList<?> observableList) {
                observableList.addListener((ListChangeListener<Object>) c -> viewBindEventHandler.changed(propertyValue, c.getList(), c.getList()));
            }
            if (value instanceof ObservableSet<?> observableSet) {
                observableSet.addListener((SetChangeListener<Object>) c -> viewBindEventHandler.changed(propertyValue, c.getSet(), c.getSet()));
            }
            if (value instanceof ObservableMap<?, ?> observableMap) {
                observableMap.addListener((MapChangeListener<Object, Object>) c -> viewBindEventHandler.changed(propertyValue, c.getMap(), c.getMap()));
            }
        }
    }

    @RequiredArgsConstructor
    public static class ViewBindEventHandler implements ChangeListener<Object> {
        private final String bindPath;
        private final DataBinder dataBinder;
        private final AbstractViewModelBindCapableController controller;

        /**
         * Collection/Map 类型是否已初始化
         * 集合类型不做代理，因此直接设置 javafx 集合对象到绑定模型，以实现直接操作集合即可直接操作视图
         */
        private boolean isInit;

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            if (newValue instanceof Collection<?> || newValue instanceof Map<?,?>) {
                if (!this.isInit) {
                    ReflectUtil.setNestedFieldValue(this.bindPath, this.controller, newValue);
                    this.isInit = true;
                }
                return;
            }
            try (GenericPropertiesContext context = this.dataBinder.getPropertyContext()) {
                if (newValue != null) {
                    String virtual = UUID.randomUUID().toString();
                    context.setProperty(virtual + '.' + bindPath, newValue.toString());
                    this.dataBinder.bind(new Instance(this.controller), virtual);
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
