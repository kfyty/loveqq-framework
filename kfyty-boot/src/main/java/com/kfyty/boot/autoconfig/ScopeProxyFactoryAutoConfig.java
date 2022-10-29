package com.kfyty.boot.autoconfig;

import com.kfyty.boot.autoconfig.support.DefaultScopeProxyFactory;
import com.kfyty.boot.autoconfig.support.PrototypeScopeProxyFactory;
import com.kfyty.boot.autoconfig.support.RefreshScopeProxyFactory;
import com.kfyty.boot.autoconfig.support.SingletonScopeProxyFactory;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnMissingBean;

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
}
