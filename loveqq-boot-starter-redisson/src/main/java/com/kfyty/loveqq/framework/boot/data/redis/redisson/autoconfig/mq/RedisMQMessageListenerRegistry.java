package com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq;

import com.kfyty.loveqq.framework.boot.data.redis.redisson.autoconfig.mq.annotation.RedisMQMessageConsumer;
import com.kfyty.loveqq.framework.core.autoconfig.BeanPostProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.AopUtil;
import com.kfyty.loveqq.framework.core.utils.ReflectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * 描述: rocketmq 消息监听器 bean
 *
 * @author kfyty725
 * @date 2024/12/09 21:54
 * @email kfyty725@hotmail.com
 */
@Slf4j
@RequiredArgsConstructor
public class RedisMQMessageListenerRegistry implements BeanPostProcessor {
    /**
     * bean 工厂
     */
    private final BeanFactory beanFactory;

    /**
     * redis 消息队列
     */
    private final Lazy<RedisMessageQueue> queue;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Object target = AopUtil.getTarget(bean);
        if (AnnotationUtil.hasAnnotation(target.getClass(), RedisMQMessageConsumer.class)) {
            for (Method method : ReflectUtil.getMethods(target.getClass())) {
                RedisMQMessageConsumer annotation = AnnotationUtil.findAnnotation(method, RedisMQMessageConsumer.class);
                if (annotation != null) {
                    ReflectiveMessageListener listener = new ReflectiveMessageListener(annotation.mode(), Lazy.of(() -> this.beanFactory.getBean(beanName)), method);
                    this.queue.get().registryMessageListener(annotation.value(), listener);
                    log.info("Registry RedisMQ message listener: '{}' with '{}' -> {}", annotation.value(), annotation.mode().name(), method);
                }
            }
        }
        return null;
    }

    @RequiredArgsConstructor
    private static class ReflectiveMessageListener implements MessageListener {
        /**
         * 消费模式
         */
        private final MessageConsumeMode mode;

        /**
         * 监听器实例
         */
        private final Lazy<Object> bean;

        /**
         * 监听器方法
         */
        private final Method method;

        @Override
        public MessageConsumeMode getMode() {
            return this.mode;
        }

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
