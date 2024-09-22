package com.kfyty.loveqq.framework.core.event;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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
    protected final String beanName;
    protected final Method listenerMethod;
    protected final Class<?> listenerType;
    protected final ApplicationContext context;

    public EventListenerAnnotationListener(String beanName, Method listenerMethod, Class<?> listenerType, ApplicationContext context) {
        this.beanName = beanName;
        this.listenerMethod = listenerMethod;
        this.listenerType = listenerType;
        this.context = context;
    }

    /**
     * 执行监听方法时应使用代理对象，否则代理失效
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onApplicationEvent(ApplicationEvent<Object> event) {
        if (this.listenerMethod == null) {
            ApplicationListener bean = this.context.getBean(this.beanName);
            bean.onApplicationEvent(event);
            return;
        }
        int index = -1;
        Object[] params = new Object[this.listenerMethod.getParameterCount()];
        for (Parameter parameter : this.listenerMethod.getParameters()) {
            index++;
            if (event.getClass().equals(parameter.getType())) {
                params[index] = event;
            }
        }
        this.invokeListener(params);
    }

    /**
     * 执行监听器
     *
     * @param params 方法参数
     */
    protected void invokeListener(Object[] params) {
        ReflectUtil.invokeMethod(this.context.getBean(this.beanName), this.listenerMethod, params);
    }
}
