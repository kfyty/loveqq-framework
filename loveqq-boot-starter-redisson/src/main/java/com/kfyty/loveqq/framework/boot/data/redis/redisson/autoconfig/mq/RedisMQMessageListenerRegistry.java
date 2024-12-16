package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.annotation.RedisMQMessageConsumer;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextOnRefresh;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

/**
 * 描述: rocketmq 消息监听器 bean
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class RedisMQMessageListenerRegistry implements ContextOnRefresh {
    /**
     * redis 消息队列
     */
    private final RedisMessageQueue queue;

    @Override
    public void onRefresh(ApplicationContext applicationContext) {
        Map<String, BeanDefinition> beanDefinitionMap = applicationContext.getBeanDefinitionWithAnnotation(RedisMQMessageConsumer.class, true);
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            for (Method method : ReflectUtil.getMethods(entry.getValue().getBeanType())) {
                RedisMQMessageConsumer annotation = AnnotationUtil.findAnnotation(method, RedisMQMessageConsumer.class);
                if (annotation != null) {
                    ReflectiveMessageListener listener = new ReflectiveMessageListener(new Lazy<>(() -> applicationContext.getBean(entry.getKey())), method);
                    this.queue.registryMessageListener(annotation.value(), listener);
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static class ReflectiveMessageListener implements MessageListener {
        /**
         * 监听器实例
         */
        private final Lazy<Object> bean;

        /**
         * 监听器方法
         */
        private final Method method;

        @Override
        public void onMessage(Object message) {
            Parameter parameter = this.method.getParameters()[0];
            Class<?> parameterType = parameter.getType();
            if (parameterType.isArray() && message.getClass().isArray() && parameterType.getComponentType() == message.getClass().getComponentType()) {
                ReflectUtil.invokeMethod(this.bean.get(), this.method, message);
                return;
            }
            if (parameterType.isInstance(message)) {
                if (Collection.class.isAssignableFrom(parameterType)) {
                    Class<?> actualType = (Class<?>) ((ParameterizedType) parameter.getParameterizedType()).getActualTypeArguments()[0];
                    if (message instanceof Collection<?> && actualType.isInstance(((Collection<?>) message).iterator().next())) {
                        ReflectUtil.invokeMethod(this.bean.get(), this.method, message);
                    }
                    return;
                }
                ReflectUtil.invokeMethod(this.bean.get(), this.method, message);
            }
        }
    }
}
