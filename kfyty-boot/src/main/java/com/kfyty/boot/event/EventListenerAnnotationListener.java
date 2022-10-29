package com.kfyty.boot.event;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.event.ApplicationEvent;
import com.kfyty.core.event.ApplicationListener;
import com.kfyty.core.utils.ReflectUtil;
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
public class EventListenerAnnotationListener implements ApplicationListener<ApplicationEvent<Object>> {
    private final String beanName;
    private final Method listenerMethod;
    private final Class<?> listenerType;
    private final ApplicationContext context;

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
    public void onApplicationEvent(ApplicationEvent<Object> event) {
        int index = -1;
        Object[] params = new Object[this.listenerMethod.getParameterCount()];
        for (Parameter parameter : this.listenerMethod.getParameters()) {
            index++;
            if(event.getClass().equals(parameter.getType())) {
                params[index] = event;
            }
        }
        ReflectUtil.invokeMethod(this.context.getBean(this.beanName), this.listenerMethod, params);
    }
}
