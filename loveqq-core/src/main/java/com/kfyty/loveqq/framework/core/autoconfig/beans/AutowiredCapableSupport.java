package com.kfyty.loveqq.framework.core.autoconfig.beans;

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
    String BEAN_NAME = "com.kfyty.loveqq.framework.core.autoconfig.beans.AutowiredCapableSupport";

    /**
     * 对 bean 执行依赖注入
     *
     * @param beanName bean nam
     * @param bean     bean 实例
     */
    void autowiredBean(String beanName, Object bean);
}
