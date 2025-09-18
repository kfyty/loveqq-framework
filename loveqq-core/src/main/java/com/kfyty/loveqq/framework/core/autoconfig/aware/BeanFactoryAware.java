package com.kfyty.loveqq.framework.core.autoconfig.aware;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;

/**
 * 描述: 设置 BeanFactory
 *
 * @author kfyty725
 * @date 2021/6/12 12:08
 * @email kfyty725@hotmail.com
 */
public interface BeanFactoryAware {
    void setBeanFactory(BeanFactory beanFactory);
}
