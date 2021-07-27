package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.utils.BeanUtil;

/**
 * 描述: factory bean
 *
 * @author kfyty
 * @date 2021/6/12 10:05
 * @email kfyty725@hotmail.com
 */
public interface FactoryBean<T> {

    default String getBeanName() {
        return BeanUtil.convert2BeanName(this.getBeanType());
    }

    Class<?> getBeanType();

    T getObject();

    default boolean isSingleton() {
        return true;
    }
}
