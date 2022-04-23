package com.kfyty.support.autoconfig.beans;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.InstantiationAwareBeanPostProcessor;

import java.util.Comparator;
import java.util.Map;

import static com.kfyty.support.autoconfig.annotation.Order.HIGHEST_PRECEDENCE;
import static com.kfyty.support.autoconfig.annotation.Order.LOWEST_PRECEDENCE;
import static com.kfyty.support.utils.BeanUtil.getBeanOrder;

/**
 * 描述: bean 定义
 *
 * @author kfyty725
 * @date 2021/5/22 11:13
 * @email kfyty725@hotmail.com
 */
public interface BeanDefinition {
    /**
     * BeanDefinition 排序比较器
     */
    Comparator<BeanDefinition> BEAN_DEFINITION_COMPARATOR = Comparator
            .comparing((BeanDefinition e) -> InstantiationAwareBeanPostProcessor.class.isAssignableFrom(e.getBeanType()) ? HIGHEST_PRECEDENCE : LOWEST_PRECEDENCE)
            .thenComparing(e -> getBeanOrder((BeanDefinition) e));

    /**
     * 单例作用域
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型作用域
     */
    String SCOPE_PROTOTYPE = "prototype";

    /**
     * bean name，唯一
     */
    String getBeanName();

    /**
     * 设置 bean name
     */
    void setBeanName(String beanName);

    /**
     * bean 的类型
     */
    Class<?> getBeanType();

    /**
     * 设置 bean 类型
     */
    void setBeanType(Class<?> beanType);

    /**
     * bean 的作用域
     */
    String getScope();

    /**
     * 设置 bean 作用域
     */
    void setScope(String scope);

    /**
     * 是否单例
     */
    boolean isSingleton();

    /**
     * 是否是自动装配的候选者
     */
    boolean isAutowireCandidate();

    /**
     * 设置是否是自动装配的候选者，只对针对类型装配有效
     */
    void setAutowireCandidate(boolean autowireCandidate);

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
