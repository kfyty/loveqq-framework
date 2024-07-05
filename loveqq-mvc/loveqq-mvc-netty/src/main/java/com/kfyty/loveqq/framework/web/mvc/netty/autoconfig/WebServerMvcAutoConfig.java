package com.kfyty.loveqq.framework.web.mvc.netty.autoconfig;

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
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.NettyWebServer;
import com.kfyty.loveqq.framework.web.mvc.netty.filter.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.netty.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.mvc.netty.request.resolver.ServerHandlerMethodReturnValueProcessor;

import java.util.List;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@Conditional(WebServerMvcAutoConfig.NettyWebServerAutoConfigCondition.class)
public class WebServerMvcAutoConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<ServerHandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<ServerHandlerMethodReturnValueProcessor> returnValueProcessors;

    @Autowired
    private WebServerProperties webServerProperties;

    @Bean
    @ConfigurationProperties("k.mvc.netty")
    public NettyServerProperties nettyServerProperties() {
        NettyServerProperties config = this.webServerProperties.copy(new NettyServerProperties());
        this.applicationContext.getBeanOfType(FilterRegistrationBean.class).values().forEach(config::addWebFilter);
        return config;
    }

    @Bean
    public DispatcherHandler dispatcherHandler(RequestMappingMatcher requestMappingMatcher) {
        DispatcherHandler dispatcherHandler = new DispatcherHandler();
        this.interceptorChain.forEach(dispatcherHandler::addInterceptor);
        this.argumentResolvers.forEach(dispatcherHandler::addArgumentResolver);
        this.returnValueProcessors.forEach(dispatcherHandler::addReturnProcessor);
        dispatcherHandler.setRequestMappingMatcher(requestMappingMatcher);
        return dispatcherHandler;
    }

    @Bean(destroyMethod = "stop")
    public NettyWebServer nettyWebServer(NettyServerProperties config, DispatcherHandler dispatcherHandler) {
        return new NettyWebServer(config, dispatcherHandler);
    }

    static class NettyWebServerAutoConfigCondition implements Condition {

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
