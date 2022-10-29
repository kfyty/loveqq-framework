package com.kfyty.boot.autoconfig.support;

import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.beans.ScopeProxyFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 描述: 单例作用域代理工厂，一般情况下复合代理时会走到这里
 *
 * @author kfyty725
 * @date 2022/10/22 10:19
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class SingletonScopeProxyFactory implements ScopeProxyFactory {

    @Override
    public Object getObject(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        return beanFactory.getBean(beanDefinition.getBeanName());
    }
}
