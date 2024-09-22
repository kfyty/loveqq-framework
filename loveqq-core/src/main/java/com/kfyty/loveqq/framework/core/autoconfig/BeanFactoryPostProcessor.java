package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

/**
 * 描述: bean 工厂后置处理器，该处理器应该在 k.factories 中定义，不应该在配置类中定义，否则可能引起配置类过早初始化而错过某些增强处理
 * 如果需要引用 bean 定义，只可以引用 bean name，禁止引用 {@link BeanDefinition}，因为作用域代理/懒加载等会修改 bean name 到 {@link BeanDefinition} 的映射
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
