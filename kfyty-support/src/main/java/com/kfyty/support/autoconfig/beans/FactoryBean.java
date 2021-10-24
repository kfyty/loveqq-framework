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

    default String getBeanName() {
        return resolveBeanName(this.getClass());
    }

    Class<?> getBeanType();

    T getObject();

    default boolean isSingleton() {
        return true;
    }
}
