package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;

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
     * 添加默认的构造器参数，参数索引从 0 开始
     * 其他的参数将从 bean 工厂获取
     */
    BeanDefinition addConstructorArgs(Class<?> argType, Object arg);

    /**
     * 获取构造器参数
     */
    Map<Class<?>, Object> getConstructArgs();

    /**
     * 获取构造器参数类型
     */
    Class<?>[] getConstructArgTypes();

    /**
     * 获取构造器参数值
     */
    Object[] getConstructArgValues();

    /**
     * 创建 bean 实例
     */
    Object createInstance(ApplicationContext context);
}
