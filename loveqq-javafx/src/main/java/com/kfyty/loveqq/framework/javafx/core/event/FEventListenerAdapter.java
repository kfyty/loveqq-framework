package com.kfyty.loveqq.framework.javafx.core.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Order;
import com.kfyty.loveqq.framework.core.autoconfig.aware.ApplicationContextAware;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;
import com.kfyty.loveqq.framework.core.event.ApplicationEvent;
import com.kfyty.loveqq.framework.core.event.ApplicationListener;
import com.kfyty.loveqq.framework.core.event.EventListenerAdapter;
import com.kfyty.loveqq.framework.core.event.EventListenerAnnotationListener;
import com.kfyty.loveqq.framework.core.support.Pair;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.LogUtil;
import javafx.scene.Node;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 描述: javafx 事件监听器工厂
 * 由于视图控制器一般为原型作用域，不会放入 bean 容器。所以放发布事件时，仍会创建新的控制器，而不是视图绑定的控制器。
 * 因此这里缓存视图和控制器的对应关系及实例，当视图销毁时，再释放实例
 *
 * @author kfyty725
 * @date 2024/2/21 11:56
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Order(Integer.MIN_VALUE)
@Component(FEventListenerAdapter.BEAN_NAME)
public class FEventListenerAdapter implements EventListenerAdapter, ApplicationContextAware, InternalPriority {
    /**
     * bean name
     */
    public static final String BEAN_NAME = "fEventListenerAdapter";

    /**
     * 上下文
     */
    private ApplicationContext applicationContext;

    /**
     * 原型作用域的控制器缓存
     */
    @Getter
    private final Map<String, Queue<Pair<Node, Object>>> viewController = new ConcurrentHashMap<>();

    /**
     * 设置上下文
     *
     * @param applicationContext 应用上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 添加控制器实例和视图的缓存
     *
     * @param beanName   bean name
     * @param view       视图
     * @param controller 控制器
     */
    public void addController(String beanName, Node view, Object controller) {
        if (!this.applicationContext.getBeanDefinition(beanName).isSingleton()) {
            this.viewController.computeIfAbsent(beanName, k -> new ConcurrentLinkedQueue<>()).add(new Pair<>(view, controller));
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public ApplicationListener<?> adapt(ApplicationListener<ApplicationEvent<?>> source, ApplicationListener<ApplicationEvent<?>> listener) {
        if (EventListenerAnnotationListener.class.isInstance(listener)) {
            return new FEventAnnotationListener((EventListenerAnnotationListener) (ApplicationListener) listener);
        }
        LogUtil.logIfDebugEnabled(log, log -> log.debug("JavaFx event listener doesn't adapt listener type of: {}", listener.getClass()));
        return listener;
    }

    @RequiredArgsConstructor
    public class FEventAnnotationListener implements ApplicationListener<ApplicationEvent<Object>> {
        /**
         * 原始监听器
         */
        protected final EventListenerAnnotationListener listener;

        /**
         * 优先使用已存在的控制器示例执行监听器
         * 当视图销毁时，控制器实例也被移除
         *
         * @see com.kfyty.loveqq.framework.javafx.core.event.ViewCloseEventListener
         */
        @Override
        public void onApplicationEvent(ApplicationEvent<Object> event) {
            Queue<Pair<Node, Object>> controllers = FEventListenerAdapter.this.viewController.get(this.listener.getBeanName());
            if (this.listener.getListenerMethod() == null || CommonUtil.empty(controllers)) {
                this.listener.onApplicationEvent(event);
                return;
            }
            for (Pair<Node, Object> pair : controllers) {
                this.listener.invokeListener(pair.getValue(), event);
            }
        }
    }
}
