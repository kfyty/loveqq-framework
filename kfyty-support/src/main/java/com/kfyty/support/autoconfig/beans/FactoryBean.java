package com.kfyty.support.autoconfig.beans;

import static com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder.resolveBeanName;

/**
 * 描述: factory bean
 *
 * @author kfyty
 * @date 2021/6/12 10:05
 * @email kfyty725@hotmail.com
 */
public interface FactoryBean<T> {
    /**
     * 默认 bean name 为 bean type 的简写
     * FactoryBean<T> 本身的 bean name 为 & + #{beanName}
     *
     * @return bean name
     */
    default String getBeanName() {
        return resolveBeanName(this.getBeanType());
    }

    /**
     * 返回 bean type
     *
     * @return bean type
     */
    Class<?> getBeanType();

    /**
     * 返回 bean 实例
     *
     * @return bean
     */
    T getObject();

    /**
     * 是否单例
     *
     * @return true if singleton
     */
    default boolean isSingleton() {
        return true;
    }
}
