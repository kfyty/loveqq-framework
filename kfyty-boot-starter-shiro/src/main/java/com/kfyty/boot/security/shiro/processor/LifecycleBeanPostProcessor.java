package com.kfyty.boot.security.shiro.processor;

import com.kfyty.core.autoconfig.BeanPostProcessor;
import com.kfyty.core.autoconfig.annotation.Component;
import com.kfyty.core.utils.ExceptionUtil;
import org.apache.shiro.event.EventBus;
import org.apache.shiro.event.EventBusAware;
import org.apache.shiro.event.Subscribe;
import org.apache.shiro.util.ClassUtils;
import org.apache.shiro.util.CollectionUtils;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;

import java.util.List;

/**
 * 描述: 执行 shiro 的生命周期
 *
 * @author kfyty725
 * @date 2024/6/06 20:55
 * @email kfyty725@hotmail.com
 */
@Component
public class LifecycleBeanPostProcessor implements BeanPostProcessor {
    private EventBus eventBus;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof EventBus) {
            this.eventBus = (EventBus) bean;
        }
        if (bean instanceof Initializable) {
            ((Initializable) bean).init();
        }
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof EventBusAware) {
            ((EventBusAware) bean).setEventBus(this.eventBus);
        } else if (isEventSubscriber(bean)) {
            this.eventBus.register(bean);
        }
        return null;
    }

    @Override
    public void postProcessBeforeDestroy(Object bean, String beanName) {
        try {
            if (bean instanceof Destroyable) {
                ((Destroyable) bean).destroy();
            }
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    private boolean isEventSubscriber(Object bean) {
        List<?> annotatedMethods = ClassUtils.getAnnotatedMethods(bean.getClass(), Subscribe.class);
        return !CollectionUtils.isEmpty(annotatedMethods);
    }
}
