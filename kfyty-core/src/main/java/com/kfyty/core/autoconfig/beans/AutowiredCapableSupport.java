package com.kfyty.core.autoconfig.beans;

/**
 * 描述: 自动注入支持
 *
 * @author kfyty725
 * @date 2021/7/3 12:58
 * @email kfyty725@hotmail.com
 */
public interface AutowiredCapableSupport {
    /**
     * bean name
     */
    String BEAN_NAME = "com.kfyty.support.autoconfig.beans.AutowiredCapableSupport";

    /**
     * 对 bean 执行依赖注入
     * 懒加载的 bean 可跳过
     *
     * @param beanName bean nam
     * @param bean     bean 实例
     */
    void autowiredBean(String beanName, Object bean);

    /**
     * 对 bean 执行依赖注入
     *
     * @param beanName    bean name
     * @param bean        bean 实例
     * @param ignoredLazy 是否暂时忽略懒加载的属性/方法注入
     */
    void autowiredBean(String beanName, Object bean, boolean ignoredLazy);

    /**
     * 注入懒加载的属性/方法 bean
     */
    void autowiredLazied();
}
