package com.kfyty.loveqq.framework.core.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.core.utils.CommonUtil;
import com.kfyty.loveqq.framework.core.utils.OgnlUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/21 18:05
 * @email kfyty725@hotmail.com
 */
@Getter
@ToString(exclude = "context")
@EqualsAndHashCode(exclude = "context")
public class EventListenerAnnotationListener implements ApplicationListener<ApplicationEvent<Object>> {
    /**
     * 响应式库是否可用
     */
    private static boolean REACTOR_AVAILABLE;

    static {
        try {
            Class.forName("org.reactivestreams.Publisher", false, EventListenerAnnotationListener.class.getClassLoader());
            REACTOR_AVAILABLE = true;
        } catch (Throwable e) {
            // ignored
        }
    }

    /**
     * bean name
     */
    protected final String beanName;

    /**
     * 监听方法，null 表示对应的实例是 {@link ApplicationListener} 类型，可直接调用
     */
    protected final Method listenerMethod;

    /**
     * 监听器类型
     */
    protected final Class<?> listenerType;

    /**
     * 监听条件
     */
    protected final String condition;

    /**
     * 应用上下文
     */
    protected final ApplicationContext context;

    public EventListenerAnnotationListener(String beanName, Method listenerMethod, Class<?> listenerType, String condition, ApplicationContext context) {
        this.beanName = beanName;
        this.listenerMethod = listenerMethod;
        this.listenerType = listenerType;
        this.condition = condition;
        this.context = context;
    }

    /**
     * 执行监听方法时应使用代理对象，否则代理失效
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onApplicationEvent(ApplicationEvent<Object> event) {
        if (this.listenerMethod != null) {
            this.invokeListener(this.context.getBean(this.beanName), event);
        } else {
            ApplicationListener listener = this.context.getBean(this.beanName);
            listener.onApplicationEvent(event);
        }
    }

    /**
     * 执行监听器
     *
     * @param target 监听器实例
     * @param event  监听事件
     */
    public void invokeListener(Object target, ApplicationEvent<Object> event) {
        int index = 0;
        Parameter[] parameters = this.listenerMethod.getParameters();
        Object[] parameterArgs = new Object[parameters.length];
        for (Parameter parameter : parameters) {
            if (BeanFactory.class.isAssignableFrom(parameter.getType())) {
                parameterArgs[index++] = this.context;
                continue;
            }
            if (event.getClass().equals(parameter.getType())) {
                parameterArgs[index++] = event;
                continue;
            }
        }
        this.invokeListener(target, parameterArgs);
    }

    /**
     * 执行监听器
     *
     * @param target 执行实例
     * @param params 方法参数
     */
    public void invokeListener(Object target, Object[] params) {
        if (this.shouldInvokeListener(target, params)) {
            Object result = ReflectUtil.invokeMethod(target, this.listenerMethod, params);
            this.handleListenerResult(result);
        }
    }

    /**
     * 是否应该执行改监听器
     *
     * @param target 执行实例
     * @param params 执行参数
     * @return true/false
     */
    public boolean shouldInvokeListener(Object target, Object[] params) {
        if (this.condition.isEmpty()) {
            return true;
        }
        Map<String, Object> conditionContext = new HashMap<>();
        Parameter[] parameters = this.listenerMethod.getParameters();
        conditionContext.put("method", this.listenerMethod);
        conditionContext.put("self", target);
        conditionContext.put("args", params);
        for (int i = 0; i < parameters.length; i++) {
            conditionContext.put("p" + i, parameters[i]);
            conditionContext.put("arg" + i, params[i]);
            conditionContext.put(parameters[i].getName(), params[i]);
        }
        return OgnlUtil.getBoolean(this.condition, conditionContext);
    }

    /**
     * 处理监听器返回值
     *
     * @param result 返回值
     */
    public void handleListenerResult(Object result) {
        if (result != null) {
            List<?> list = CommonUtil.toList(result);
            for (Object value : list) {
                this.doHandleListenerResult(value);
            }
        }
    }

    public void doHandleListenerResult(Object value) {
        if (value instanceof ApplicationEvent<?>) {
            this.context.publishEvent((ApplicationEvent<?>) value);
            return;
        }
        if (value instanceof CompletionStage<?>) {
            ((CompletionStage<?>) value).whenComplete((retValue, ex) -> Mapping.from(ex).whenNull(() -> this.handleListenerResult(retValue)));
            return;
        }
        if (REACTOR_AVAILABLE) {
            if (value instanceof Mono<?>) {
                ((Mono<?>) value).doOnNext(this::handleListenerResult).subscribe();
                return;
            }
            if (value instanceof Flux<?>) {
                ((Flux<?>) value).doOnNext(this::handleListenerResult).subscribe();
                return;
            }
        }
    }
}
