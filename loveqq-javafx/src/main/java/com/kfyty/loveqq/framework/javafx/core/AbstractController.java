package com.kfyty.loveqq.framework.javafx.core;

import com.kfyty.loveqq.framework.core.autoconfig.env.DataBinder;
import com.kfyty.loveqq.framework.core.support.Instance;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.IOC;
import com.kfyty.loveqq.framework.javafx.core.annotation.FController;
import com.kfyty.loveqq.framework.javafx.core.proxy.ViewModelBindProxy;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 描述: 抽象控制器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractController<View extends Parent> extends AbstractViewModelBindCapableController implements LifeCycleController {
    /**
     * 子窗口索引 key 生成器
     */
    protected static final Function<String, String> CHILD_VIEW_INDEX_KEY = id -> id + "_index";

    /**
     * 子窗口索引映射
     */
    protected static final Function<Parent, Integer> CHILD_VIEW_INDEX_MAP = v -> (Integer) v.getProperties().get(CHILD_VIEW_INDEX_KEY.apply(v.getId()));

    /**
     * 是否已经初始化
     */
    protected boolean isInit;

    /**
     * 绑定标记
     */
    protected boolean bindMark;

    /**
     * 控制器所属视图
     */
    protected View view;

    /**
     * 控制器所属窗口
     */
    protected Stage window;

    /**
     * 所属父控制器
     */
    protected AbstractController<?> parent;

    /**
     * 子窗口
     */
    protected Map<String, List<Parent>> children;

    /**
     * 父窗口传来的参数上下文
     * 也可以自行放入上下文参数
     */
    protected volatile Map<String, Object> context;

    /**
     * 默认构造器
     */
    public AbstractController() {
        this.children = new ConcurrentHashMap<>();
    }

    /**
     * 打开新窗口
     *
     * @param childWindowControllerClass 子窗口控制类型
     */
    public <V extends Parent, T extends AbstractController<V>> T openWindow(Class<T> childWindowControllerClass) {
        return this.openWindow(childWindowControllerClass, (Map<String, Object>) null);
    }

    /**
     * 打开新窗口
     *
     * @param childWindowControllerClass 子窗口控制类型
     * @param parameters                 子窗口参数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <V extends Parent, T extends AbstractController<V>> T openWindow(Class<T> childWindowControllerClass, String parameters) {
        Map parameterMap = CommonUtil.resolveURLParameters(parameters, PARAMETER_PREFIX);
        return this.openWindow(childWindowControllerClass, (Map<String, Object>) parameterMap);
    }

    /**
     * 打开新窗口
     *
     * @param childWindowControllerClass 子窗口控制类型
     * @param parameters                 子窗口参数
     */
    public <V extends Parent, T extends AbstractController<V>> T openWindow(Class<T> childWindowControllerClass, Map<String, Object> parameters) {
        FController fController = AnnotationUtil.findAnnotation(childWindowControllerClass, FController.class);
        Stage child = IOC.getBean(fController.value());
        return this.openWindow(child, parameters);
    }

    /**
     * 打开新窗口
     *
     * @param child 窗口
     */
    public <V extends Parent, T extends AbstractController<V>> T openWindow(Stage child) {
        return this.openWindow(child, (Map<String, Object>) null);
    }

    /**
     * 打开新窗口，并传参到子窗口
     *
     * @param child     窗口
     * @param parameter url 风格参数
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <V extends Parent, T extends AbstractController<V>> T openWindow(Stage child, String parameter) {
        Map parameters = CommonUtil.resolveURLParameters(parameter, PARAMETER_PREFIX);
        return this.openWindow(child, (Map<String, Object>) parameters);
    }

    /**
     * 打开新窗口，并传参到子窗口
     *
     * @param child      窗口
     * @param parameters 参数
     */
    @SuppressWarnings("unchecked")
    public <V extends Parent, T extends AbstractController<V>> T openWindow(Stage child, Map<String, Object> parameters) {
        Parent component = child.getScene().getRoot();
        Object componentController = this.getController(component);
        if (!(componentController instanceof AbstractController<?>)) {
            throw new IllegalArgumentException("The controller is not subclass of " + AbstractController.class.getName());
        }

        AbstractController<?> controller = (AbstractController<?>) componentController;

        if (controller.isInit()) {
            controller.show();
            this.addChild(component.getId(), component);
            return (T) controller;
        }

        // 数据绑定
        if (CommonUtil.notEmpty(parameters)) {
            DataBinder dataBinder = this.getDataBinder();
            parameters.forEach(controller::addParameters);
            parameters.forEach((k, v) -> dataBinder.setProperty(k, String.valueOf(v)));
            dataBinder.bind(new Instance(controller), PARAMETER_PREFIX);
            ViewModelBindProxy.triggerViewBind(controller);
        }

        // 注册子窗口
        this.registerChild(child);
        controller.setInit(true);

        // 显示窗口
        FController annotation = AnnotationUtil.findAnnotation(componentController, FController.class);
        if (annotation.main() || annotation.show()) {
            controller.show();
        }

        return (T) controller;
    }

    public int getIndex() {
        Integer index = CHILD_VIEW_INDEX_MAP.apply(this.view);
        return index == null ? 0 : index;
    }

    public <V extends Parent, T extends AbstractController<V>> T getController(Parent component) {
        return ((FXMLLoader) component.getProperties().get(component)).getController();
    }

    public void addParameters(String key, Object value) {
        if (this.context == null) {
            synchronized (this) {
                if (this.context == null) {
                    this.context = new ConcurrentHashMap<>();
                }
            }
        }
        this.context.put(key, value);
    }

    public void addChild(String name, Parent view) {
        List<Parent> nodes = this.children.computeIfAbsent(name, k -> new ArrayList<>());
        synchronized (nodes) {
            if (nodes.stream().noneMatch(e -> e == view)) {
                nodes.add(view);
                view.getProperties().put(CHILD_VIEW_INDEX_KEY.apply(view.getId()), nodes.size() - 1);
            }
        }
    }

    public void removeChild(String name) {
        this.removeChild(name, 0);
    }

    public void removeChild(String name, Parent view) {
        List<Parent> nodes = this.children.get(name);
        if (nodes != null) {
            synchronized (nodes) {
                nodes.removeIf(e -> e == view);
            }
        }
    }

    public void removeChild(String name, int index) {
        List<Parent> nodes = this.children.get(name);
        if (nodes != null) {
            synchronized (nodes) {
                nodes.removeIf(v -> CHILD_VIEW_INDEX_MAP.apply(v) == index);
            }
        }
    }

    public <V extends Parent> V getChild(String name) {
        return this.getChild(name, 0);
    }

    @SuppressWarnings("unchecked")
    public <V extends Parent> V getChild(String name, int index) {
        List<Parent> nodes = this.children.get(name);
        if (nodes != null) {
            synchronized (nodes) {
                return (V) nodes.stream().filter(v -> CHILD_VIEW_INDEX_MAP.apply(v) == index).findAny().orElse(null);
            }
        }
        return null;
    }

    public <V extends Parent, T extends AbstractController<V>> T getChildController(Class<T> clazz) {
        return this.getChildController(clazz, 0);
    }

    public <V extends Parent, T extends AbstractController<V>> T getChildController(Class<T> clazz, int index) {
        FController fController = AnnotationUtil.findAnnotation(clazz, FController.class);
        return this.getChildController(fController.value(), index);
    }

    public <V extends Parent, T extends AbstractController<V>> T getChildController(String name) {
        return this.getChildController(name, 0);
    }

    public <V extends Parent, T extends AbstractController<V>> T getChildController(String name, int index) {
        Parent child = this.getChild(name, index);
        if (child == null) {
            return null;
        }
        return this.getController(child);
    }

    public void hide() {
        if (this.window != null) {
            this.window.hide();
        }
    }

    public void show() {
        if (this.window != null) {
            this.window.show();
        }
    }

    public void close() {
        if (this.window != null) {
            Optional.ofNullable(this.window.getOnCloseRequest()).ifPresent(onClose -> onClose.handle(new WindowEvent(this.window, WindowEvent.WINDOW_CLOSE_REQUEST)));
            this.window.close();
        }
    }

    @Override
    public boolean isMarkBind() {
        return this.bindMark;
    }

    @Override
    public void markBind() {
        this.bindMark = true;
    }

    @Override
    public void unmarkBind() {
        this.bindMark = false;
    }
}
