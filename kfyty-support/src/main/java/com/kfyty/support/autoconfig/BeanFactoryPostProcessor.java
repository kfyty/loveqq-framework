package com.kfyty.support.autoconfig;

import com.kfyty.support.autoconfig.beans.BeanFactory;

/**
 * 描述: bean 工厂后置处理器
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
