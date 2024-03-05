package com.kfyty.javafx.core.factory;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.event.EventListenerAnnotationListener;
import com.kfyty.core.event.EventListenerAnnotationListenerFactory;
import com.kfyty.core.support.Pair;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import javafx.scene.Node;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 描述: javafx 事件监听器工厂
 * 由于视图控制器一般为原型作用域，不会放入 bean 容器。所以放发布事件时，扔会创建新的控制器，而不是视图绑定的控制器。
 * 因此这里缓存视图和控制器的对应关系及实例，当视图销毁时，再释放实例
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
public class FEventListenerFactory implements EventListenerAnnotationListenerFactory, ApplicationContextAware {
    /**
     * 上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 原型作用域的控制器缓存
     */
    @Getter
    private final Map<String, Queue<Pair<Node, Object>>> viewController = new ConcurrentHashMap<>();

    public void addController(String beanName, Node view, Object controller) {
        if (!this.applicationContext.getBeanDefinition(beanName).isSingleton()) {
            this.viewController.computeIfAbsent(beanName, k -> new ConcurrentLinkedQueue<>()).add(new Pair<>(view, controller));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public ApplicationListener<?> createEventListener(String beanName, Method listenerMethod, Class<?> listenerType) {
        return new FEventAnnotationListener(beanName, listenerMethod, listenerType, this.applicationContext);
    }

    public class FEventAnnotationListener extends EventListenerAnnotationListener {

        public FEventAnnotationListener(String beanName, Method listenerMethod, Class<?> listenerType, ApplicationContext context) {
            super(beanName, listenerMethod, listenerType, context);
        }

        @Override
        protected void invokeListener(Object[] params) {
            Queue<Pair<Node, Object>> controllers = viewController.get(this.beanName);
            if (CommonUtil.empty(controllers)) {
                super.invokeListener(params);
                return;
            }
            for (Pair<Node, Object> pair : controllers) {
                ReflectUtil.invokeMethod(pair.getValue(), this.listenerMethod, params);
            }
        }
    }
}
