package com.kfyty.core.autoconfig;

import com.kfyty.core.autoconfig.beans.BeanFactory;

/**
 * 描述: bean 工厂前置处理器
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
public interface BeanFactoryPreProcessor {
    /**
     * 是否进行依赖注入
     *
     * @return 默认 false
     */
    default boolean allowAutowired() {
        return false;
    }

    /**
     * bean 工厂前置处理器
     *
     * @param beanFactory bean 工厂
     */
    void preProcessBeanFactory(BeanFactory beanFactory);
}
