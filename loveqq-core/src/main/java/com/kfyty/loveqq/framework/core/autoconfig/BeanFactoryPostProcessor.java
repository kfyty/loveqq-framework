package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

/**
 * 描述: bean 工厂后置处理器，该处理器应该在 k.factories 中定义，不应该在配置类中定义，否则可能引起配置类过早初始化而错过某些增强处理
 *
 * @author kfyty
 * @date 2022/10/23 15:30
 * @email kfyty725@hotmail.com
 */
public interface BeanFactoryPostProcessor {
    /**
     * bean 工厂后置处理器
     * 此时全部 bean 定义已加载
     *
     * @param beanFactory bean 工厂
     */
    void postProcessBeanFactory(BeanFactory beanFactory);
}
