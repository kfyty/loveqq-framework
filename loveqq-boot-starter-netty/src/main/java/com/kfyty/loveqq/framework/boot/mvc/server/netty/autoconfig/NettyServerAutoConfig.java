package com.kfyty.loveqq.framework.boot.mvc.server.netty.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.server.netty.NettyWebServer;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.resource.DefaultResourceResolver;
import com.kfyty.loveqq.framework.boot.mvc.server.netty.resource.ResourceResolver;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.Condition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.ConditionContext;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.Filter;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterRegistrationBean;

/**
 * 描述: netty 服务器自动配置
 *
 * @author kfyty725
 * @date 2024/7/7 11:29
 * @email kfyty725@hotmail.com
 */
@Configuration
@Conditional(NettyServerAutoConfig.NettyServerAutoConfigCondition.class)
public class NettyServerAutoConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebServerProperties webServerProperties;

    @Bean
    @ConditionalOnMissingBean
    public ResourceResolver defaultResourceResolver() {
        return new DefaultResourceResolver();
    }

    @Bean
    @ConfigurationProperties("k.mvc.netty")
    public NettyProperties nettyServerProperties(ResourceResolver resourceResolver) {
        NettyProperties config = this.webServerProperties.copy(new NettyProperties());
        config.setResourceResolver(resourceResolver);
        this.applicationContext.getBeanOfType(Filter.class).values().forEach(config::addWebFilter);
        this.applicationContext.getBeanOfType(FilterRegistrationBean.class).values().forEach(config::addWebFilter);
        return config;
    }

    @Bean(destroyMethod = "stop")
    public NettyWebServer nettyWebServer(NettyProperties config, DispatcherHandler dispatcherHandler) {
        return new NettyWebServer(config, dispatcherHandler);
    }

    static class NettyServerAutoConfigCondition implements Condition {

        @Override
        public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
            BeanFactory beanFactory = context.getBeanFactory();
            if (beanFactory instanceof ConfigurableApplicationContext) {
                Class<?> primarySource = ((ConfigurableApplicationContext) beanFactory).getPrimarySource();
                return AnnotationUtil.hasAnnotation(primarySource, EnableWebMvc.class);
            }
            return false;
        }
    }
}
