package com.kfyty.loveqq.framework.boot.autoconfig;

import com.kfyty.loveqq.framework.boot.autoconfig.support.DefaultScopeProxyFactory;
import com.kfyty.loveqq.framework.boot.autoconfig.support.PrototypeScopeProxyFactory;
import com.kfyty.loveqq.framework.boot.autoconfig.support.RefreshScopeProxyFactory;
import com.kfyty.loveqq.framework.boot.autoconfig.support.SingletonScopeProxyFactory;
import com.kfyty.loveqq.framework.boot.autoconfig.support.ThreadScopeProxyFactory;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/22 10:10
 * @email kfyty725@hotmail.com
 */
@Configuration
public class ScopeProxyFactoryAutoConfig {
    @Bean
    @ConditionalOnMissingBean(name = "scopeProxyFactory")
    public DefaultScopeProxyFactory scopeProxyFactory() {
        return new DefaultScopeProxyFactory();
    }

    @ConditionalOnMissingBean
    @Bean(BeanDefinition.SCOPE_SINGLETON)
    public SingletonScopeProxyFactory singletonScopeProxyFactory() {
        return new SingletonScopeProxyFactory();
    }

    @ConditionalOnMissingBean
    @Bean(BeanDefinition.SCOPE_PROTOTYPE)
    public PrototypeScopeProxyFactory prototypeScopeProxyFactory() {
        return new PrototypeScopeProxyFactory();
    }

    @ConditionalOnMissingBean
    @Bean(BeanDefinition.SCOPE_REFRESH)
    public RefreshScopeProxyFactory refreshScopeProxyFactory() {
        return new RefreshScopeProxyFactory();
    }

    @ConditionalOnMissingBean
    @Bean(BeanDefinition.SCOPE_THREAD)
    public ThreadScopeProxyFactory threadScopeProxyFactory() {
        return new ThreadScopeProxyFactory();
    }
}
