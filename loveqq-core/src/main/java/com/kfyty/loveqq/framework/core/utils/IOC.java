package com.kfyty.loveqq.framework.core.utils;

import com.kfyty.loveqq.framework.core.autoconfig.BeanFactoryPreProcessor;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

/**
 * 描述: ioc 便捷操作
 *
 * @author kfyty725
 * @date 2023/4/17 14:27
 * @email kfyty725@hotmail.com
 */
public class IOC implements BeanFactoryPreProcessor {
    private static BeanFactory beanFactory;

    @Override
    public void preProcessBeanFactory(BeanFactory beanFactory) {
        IOC.beanFactory = beanFactory;
    }

    /**
     * 获取 bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return beanFactory.getBean(clazz);
    }

    /**
     * 获取 bean
     */
    public static <T> T getBean(String name) {
        return beanFactory.getBean(name);
    }
}
