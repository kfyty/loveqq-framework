package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

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
     * 主要是内置的默认前置处理器不可使用，它们是进行 bean 工厂初始化的处理器
     *
     * @return 默认 true
     */
    default boolean allowAutowired() {
        return true;
    }

    /**
     * bean 工厂前置处理器
     *
     * @param beanFactory bean 工厂
     */
    void preProcessBeanFactory(BeanFactory beanFactory);
}
