package com.kfyty.loveqq.framework.core.autoconfig;

/**
 * 描述: bean 后置处理器
 *
 * @author fyty
 * @date 2021/5/29 14:29
 * @email kfyty725@hotmail.com
 */
public interface BeanPostProcessor {

    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    default void postProcessBeforeDestroy(Object bean, String beanName) {

    }
}
