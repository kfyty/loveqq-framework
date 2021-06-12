package com.kfyty.support.autoconfig.beans;

/**
 * 描述: factory bean
 *
 * @author kfyty
 * @date 2021/6/12 10:05
 * @email kfyty725@hotmail.com
 */
public interface FactoryBean<T> {

    Class<?> getBeanType();

    T getObject();
}
