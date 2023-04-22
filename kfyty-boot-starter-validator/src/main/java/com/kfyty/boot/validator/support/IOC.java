package com.kfyty.boot.validator.support;

/**
 * 描述: ioc
 *
 * @author kfyty725
 * @date 2023/4/17 14:27
 * @email kfyty725@hotmail.com
 */
@FunctionalInterface
public interface IOC {
    /**
     * 获取 bean
     */
    <T> T getBean(Class<T> clazz);
}
