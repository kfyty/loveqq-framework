package com.kfyty.loveqq.framework.javafx.core;

import com.kfyty.loveqq.framework.boot.context.env.DefaultDataBinder;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;
import com.kfyty.loveqq.framework.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.loveqq.framework.core.support.Instance;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import com.kfyty.loveqq.framework.javafx.core.annotation.FView;
import com.kfyty.loveqq.framework.javafx.core.proxy.ViewModelBindProxy;
import com.kfyty.loveqq.framework.javafx.core.utils.ViewModelBindUtil;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.UUID;

import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.findAnnotations;
import static com.kfyty.loveqq.framework.core.utils.AnnotationUtil.flatRepeatableAnnotation;

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
        DataBinder binder = this.dataBinder.clone();
        if (binder instanceof DefaultDataBinder dataBinder) {
            dataBinder.setIgnoreInvalidFields(true);
        }
        return binder;
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

    /**
     * 初始化视图模型绑定
     */
    protected void initViewBind() {
        DataBinder dataBinder = this.getDataBinder();
        Field[] fields = ReflectUtil.getFields(AopUtil.getTargetClass(this));
        for (Field field : fields) {
            if (field.getDeclaringClass() == Object.class || ReflectUtil.isStaticFinal(field.getModifiers())) {
                continue;
            }
            FView[] annotations = flatRepeatableAnnotation(findAnnotations(field), e -> e.annotationType() == FView.class, FView[]::new);
            for (FView view : annotations) {
                this.initViewBind(field, view, dataBinder);
            }
        }
    }

    /**
     * 初始化视图模型绑定
     *
     * @param field      标注 {@link FView} 的字段
     * @param fview      {@link FView}
     * @param dataBinder 数据绑定器
     */
    protected void initViewBind(Field field, FView fview, DataBinder dataBinder) {
        String[] split = fview.value().split(":");
        String property = split[0];
        String bindPath = split[1];
        Node viewNode = (Node) ReflectUtil.getFieldValue(this, field);
        ObservableValue<?> view = ViewModelBindUtil.resolveView(property, viewNode, fview);

        this.initModelToViewBind(view, bindPath);
        this.initViewToModelBind(view, bindPath, dataBinder);
    }

    /**
     * 初始化模型到视图的绑定
     *
     * @param view     可观测的视图
     * @param bindPath 绑定模型路径
     */
    protected void initModelToViewBind(ObservableValue<?> view, String bindPath) {
        // 获取绑定的属性
        Pair<Field, Object> fieldValuePair = ViewModelBindUtil.resolveModel(this, bindPath);
        Field modelField = fieldValuePair.getKey();
        Object modelValue = fieldValuePair.getValue();

        // 空值时初始化一个默认值
        if (modelValue == null) {
            modelValue = ReflectUtil.newInstance(modelField.getType());
        }

        // 添加模型绑定代理
        ViewModelBindProxy proxy = ViewModelBindUtil.bindModelProxy(this, modelField, modelValue);
        proxy.addBindView(bindPath, view);
    }

    /**
     * 初始化视图到模型的绑定
     *
     * @param view       可观测的视图
     * @param bindPath   绑定模型路径
     * @param dataBinder 数据绑定器
     */
    protected void initViewToModelBind(ObservableValue<?> view, String bindPath, DataBinder dataBinder) {
        ViewBindEventHandler viewBindEventHandler = new ViewBindEventHandler(bindPath, dataBinder, this);

        view.addListener(viewBindEventHandler);

        if (view instanceof ObjectProperty<?> objectProperty) {
            Object value = objectProperty.getValue();
            if (value instanceof ObservableArray<?>) {
                throw new UnsupportedOperationException("array bind doesn't support yet: " + bindPath);
            }
            if (value instanceof ObservableList<?> observableList) {
                observableList.addListener((ListChangeListener<Object>) c -> viewBindEventHandler.changed(view, c.getList(), c.getList()));
            }
            if (value instanceof ObservableSet<?> observableSet) {
                observableSet.addListener((SetChangeListener<Object>) c -> viewBindEventHandler.changed(view, c.getSet(), c.getSet()));
            }
            if (value instanceof ObservableMap<?, ?> observableMap) {
                observableMap.addListener((MapChangeListener<Object, Object>) c -> viewBindEventHandler.changed(view, c.getMap(), c.getMap()));
            }
        }
    }

    @RequiredArgsConstructor
    public static class ViewBindEventHandler implements ChangeListener<Object> {
        /**
         * 模型绑定路径
         */
        private final String bindPath;

        /**
         * 数据绑定器
         */
        private final DataBinder dataBinder;

        /**
         * 控制器
         */
        private final AbstractViewModelBindCapableController controller;

        /**
         * Collection/Map 类型是否已初始化
         * 集合类型不做代理，因此直接设置 javafx 集合对象到绑定模型，以实现直接操作集合即可直接操作视图
         * 所以 Collection/Map 类型的模型只能使用接口，而不是实现类
         */
        private boolean isInit;

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            if (newValue instanceof Collection<?> || newValue instanceof Map<?, ?>) {
                if (!this.isInit) {
                    ReflectUtil.setNestedFieldValue(this.bindPath, this.controller, newValue);
                    this.isInit = true;
                }
                return;
            }
            try (GenericPropertiesContext context = this.dataBinder.getPropertyContext()) {
                if (newValue != null) {
                    String virtual = UUID.randomUUID().toString();
                    context.setProperty(virtual + '.' + this.bindPath, newValue.toString());
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
