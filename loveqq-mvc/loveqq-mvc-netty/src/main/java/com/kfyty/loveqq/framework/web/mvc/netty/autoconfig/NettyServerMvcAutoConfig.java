package com.kfyty.loveqq.framework.web.mvc.netty.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.netty.ServerWebServer;

import java.util.List;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(ServerWebServer.class)
public class NettyServerMvcAutoConfig {
    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Bean
    public DispatcherHandler dispatcherHandler(RequestMappingMatcher requestMappingMatcher,
                                               @Value("${k.server.view.prefix:}") String prefix,
                                               @Value("${k.server.view.suffix:}") String suffix) {
        DispatcherHandler dispatcherHandler = new DispatcherHandler();
        dispatcherHandler.setInterceptorChains(this.interceptorChain);
        dispatcherHandler.setArgumentResolvers(this.argumentResolvers);
        dispatcherHandler.setReturnValueProcessors(this.returnValueProcessors);
        dispatcherHandler.setRequestMappingMatcher(requestMappingMatcher);
        dispatcherHandler.setPrefix(prefix);
        dispatcherHandler.setSuffix(suffix);
        return dispatcherHandler;
    }
}
