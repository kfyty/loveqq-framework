package com.kfyty.javafx.core;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.env.DataBinder;
import com.kfyty.core.autoconfig.env.GenericPropertiesContext;
import com.kfyty.core.support.Instance;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import com.kfyty.javafx.core.proxy.ViewModelBindProxy;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 描述: 抽象控制器
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@Data
public abstract class AbstractController implements ViewBindCapableController, LifeCycleController {
    /**
     * 参数key前缀
     */
    protected static final String PARAMETER_PREFIX = "controller";

    /**
     * 默认的泛型配置属性解析器
     */
    protected static final Class<?> DEFAULT_GENERIC_PROPERTIES_CONTEXT_CLASS = ReflectUtil.load("com.kfyty.boot.context.env.DefaultGenericPropertiesContext");

    /**
     * 数据绑定器
     *
     * @see this#createDataBinder()
     */
    @Autowired
    private DataBinder dataBinder;

    /**
     * 是否已经初始化
     */
    protected boolean isInit;

    /**
     * 控制器所属视图
     */
    protected Node view;

    /**
     * 控制器所属窗口
     */
    protected Stage window;

    /**
     * 子窗口
     */
    protected Map<String, List<Node>> children;

    /**
     * 父窗口传来的参数上下文
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
     * @param child 窗口
     */
    public <T extends AbstractController> T openWindow(Stage child) {
        return this.openWindow(child, null);
    }

    /**
     * 打开新窗口，并传参到子窗口
     *
     * @param child     窗口
     * @param parameter url 风格参数
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractController> T openWindow(Stage child, String parameter) {
        Parent component = child.getScene().getRoot();
        Object componentController = ((FXMLLoader) component.getProperties().get(component)).getController();
        if (!(componentController instanceof AbstractController controller)) {
            throw new IllegalArgumentException("The controller is not subclass of " + AbstractController.class.getName());
        }
        if (controller.isInit()) {
            controller.show();
            this.addChild(component.getId(), component);
            return (T) controller;
        }

        // 参数解析
        List<String> split = parameter == null ? Collections.emptyList() : CommonUtil.split(parameter, "&");
        for (String params : split) {
            String[] paramPair = params.split("=");
            controller.addParameters(PARAMETER_PREFIX + '.' + paramPair[0], paramPair[1]);
        }

        // 数据绑定
        DataBinder dataBinder = this.createDataBinder();
        if (CommonUtil.notEmpty(parameter) && CommonUtil.notEmpty(controller.context)) {
            controller.context.forEach((k, v) -> dataBinder.setProperty(k, v.toString()));
            dataBinder.bind(new Instance(controller), PARAMETER_PREFIX);
            ViewModelBindProxy.triggerViewBind(controller);
        }
        this.registerChild(child);
        controller.setInit(true);
        return (T) controller;
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

    public void addChild(String name, Node view) {
        List<Node> nodes = this.children.computeIfAbsent(name, k -> new ArrayList<>());
        synchronized (nodes) {
            if (nodes.stream().noneMatch(e -> e == view)) {
                nodes.add(view);
            }
        }
    }

    public void removeChild(String name) {
        this.removeChild(name, 0);
    }

    public void removeChild(String name, Node view) {
        List<Node> nodes = this.children.get(name);
        if (nodes != null) {
            synchronized (nodes) {
                nodes.removeIf(e -> e == view);
            }
        }
    }

    public void removeChild(String name, int index) {
        List<Node> nodes = this.children.get(name);
        if (nodes != null) {
            synchronized (nodes) {
                if (index < nodes.size()) {
                    nodes.remove(index);
                }
            }
        }
    }

    public <T extends Node> T getChild(String name) {
        return this.getChild(name, 0);
    }

    @SuppressWarnings("unchecked")
    public <T extends Node> T getChild(String name, int index) {
        List<Node> nodes = this.children.get(name);
        if (nodes != null) {
            synchronized (nodes) {
                if (index < nodes.size()) {
                    return (T) nodes.get(index);
                }
            }
        }
        return null;
    }

    public <T> T getChildController(String name) {
        return this.getChildController(name, 0);
    }

    public <T> T getChildController(String name, int index) {
        return this.getChildController(name, null, index);
    }

    public <T> T getChildController(String name, Class<T> clazz) {
        return this.getChildController(name, clazz, 0);
    }

    public <T> T getChildController(String name, Class<T> clazz, int index) {
        Node child = this.getChild(name, index);
        if (child == null) {
            return null;
        }
        FXMLLoader fxmlLoader = (FXMLLoader) child.getProperties().get(child);
        return fxmlLoader.getController();
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

    protected DataBinder createDataBinder() {
        DataBinder dataBinder = this.dataBinder.clone();
        GenericPropertiesContext propertiesContext = this.createPropertiesContext();
        propertiesContext.setDataBinder(dataBinder);
        dataBinder.setPropertyContext(propertiesContext);
        return dataBinder;
    }

    protected GenericPropertiesContext createPropertiesContext() {
        return (GenericPropertiesContext) ReflectUtil.newInstance(DEFAULT_GENERIC_PROPERTIES_CONTEXT_CLASS);
    }
}
