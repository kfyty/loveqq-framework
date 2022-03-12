package com.kfyty.boot.context.factory;

import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.beans.AutowiredCapableSupport;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.exception.BeansException;
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
        if (!(bean instanceof ApplicationContext)) {
            this.autowiredCapableSupport.doAutowiredBean(bean);
        }
    }

    public void doAutowiredLazy() {
        if (this.autowiredCapableSupport == null) {
            throw new BeansException("no bean instance found of type: " + AutowiredCapableSupport.class);
        }
        this.autowiredCapableSupport.doAutowiredLazy();
    }

    @Override
    public void close() {
        super.close();
        this.autowiredCapableSupport = null;
    }
}
