package com.kfyty.support.autoconfig.beans;

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
    String BEAN_NAME = "autowiredCapableSupport";

    /**
     * 对 bean 执行依赖注入
     * 懒加载的 bean 可跳过
     * @param bean bean 实例
     */
    void doAutowiredBean(Object bean);

    /**
     * 注入懒加载的 bean
     */
    void doAutowiredLazy();
}
