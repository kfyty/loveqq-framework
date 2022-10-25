package com.kfyty.support.autoconfig.beans;

/**
 * 描述: factory bean
 *
 * @author kfyty
 * @date 2021/6/12 10:05
 * @email kfyty725@hotmail.com
 */
public interface FactoryBean<T> {
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
     * 返回 bean 作用域
     *
     * @return {@link BeanDefinition}
     */
    default String getScope() {
        return BeanDefinition.SCOPE_SINGLETON;
    }
}
