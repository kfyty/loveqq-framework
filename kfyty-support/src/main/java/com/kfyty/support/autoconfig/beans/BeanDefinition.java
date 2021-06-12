package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ConfigurableContext;

import java.util.Map;

/**
 * 描述: bean 定义
 *
 * @author kfyty725
 * @date 2021/5/22 11:13
 * @email kfyty725@hotmail.com
 */
public interface BeanDefinition {
    /**
     * bean name，唯一
     */
    String getBeanName();

    /**
     * bean 的类型
     */
    Class<?> getBeanType();

    /**
     * 添加构造器参数
     */
    BeanDefinition addConstructorArgs(Class<?> argType, Object arg);

    /**
     * 获取构造器参数
     */
    Map<Class<?>, Object> getConstructArgs();

    /**
     * 生成 bean 实例
     */
    Object createInstance(ConfigurableContext context);
}
