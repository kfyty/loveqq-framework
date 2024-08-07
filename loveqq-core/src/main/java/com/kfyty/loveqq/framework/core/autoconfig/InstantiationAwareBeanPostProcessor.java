package com.kfyty.loveqq.framework.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.internal.InternalPriority;

/**
 * 描述: bean 实例化处理器
 *
 * @author kfyty725
 * @date 2021/6/17 18:24
 * @email kfyty725@hotmail.com
 */
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor, InternalPriority {
    /**
     * bean 实例化之后立即调用，此时可能还未注入属性
     * 返回值若不为空，则替换原 bean
     */
    default Object postProcessAfterInstantiation(Object bean, String beanName, BeanDefinition beanDefinition) {
        return null;
    }
}
