package com.kfyty.loveqq.framework.core.autoconfig.beans;

/**
 * 描述: factory bean
 *
 * @author kfyty
 * @date 2021/6/12 10:05
 * @email kfyty725@hotmail.com
 * @see FactoryBeanDefinition
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
     * 返回该工厂 Bean 是否是单例模式
     * 如果返回 true，则每次使用相同的实例调用 {@link this#getObject()} 返回真实 bean，不影响真实 bean 的作用域
     * 如果返回 false，则该工厂 bean 的作用域跟随真实 bean 的作用域
     *
     * @return true if singleton
     */
    default boolean isSingleton() {
        return true;
    }

    /**
     * 该工厂 bean 生产的 bean 是否应该应用完整的生命周期
     * 某些实现不应用应用，因为仅仅是代理
     *
     * @return true/false
     */
    default boolean shouldApplyLifecycle() {
        return true;
    }
}
