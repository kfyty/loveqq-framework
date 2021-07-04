package com.kfyty.boot.context.factory;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 支持依赖注入的 bean 工厂
 *
 * @author kfyty725
 * @date 2021/7/3 10:59
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class AbstractAutowiredBeanFactory extends AbstractBeanFactory {
    @Autowired
    protected AutowiredCapableSupport autowiredCapableSupport;

    @Override
    public Object doCreateBean(BeanDefinition beanDefinition) {
        return beanDefinition.createInstance(this.applicationContext);
    }

    @Override
    public void doAutowiredBean(String beanName, Object bean) {
        if(bean instanceof ApplicationContext) {
            return;
        }
        this.autowiredCapableSupport.doAutowiredBean(bean);
    }
}
