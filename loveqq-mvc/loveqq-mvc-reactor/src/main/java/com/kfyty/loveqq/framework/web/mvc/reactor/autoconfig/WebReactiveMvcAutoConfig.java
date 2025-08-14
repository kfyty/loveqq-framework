package com.kfyty.loveqq.framework.web.mvc.reactor.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.web.core.handler.ExceptionHandler;
import com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.route.RouteMatcher;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.reactor.DispatcherHandler;
import com.kfyty.loveqq.framework.web.mvc.reactor.ReactiveWebServer;
import com.kfyty.loveqq.framework.web.mvc.reactor.request.support.ReactiveWriter;

import java.util.List;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(ReactiveWebServer.class)
public class WebReactiveMvcAutoConfig {
    @Autowired
    private ReactiveWriter reactiveWriter;

    @Autowired
    private List<RouteMatcher> routeMatchers;

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Autowired(required = false)
    private List<ExceptionHandler> exceptionHandlers;

    @Bean
    public DispatcherHandler dispatcherHandler(@Value("${k.server.view.prefix:}") String prefix,
                                               @Value("${k.server.view.suffix:}") String suffix) {
        DispatcherHandler dispatcherHandler = new DispatcherHandler();
        dispatcherHandler.setReactiveWriter(this.reactiveWriter);
        dispatcherHandler.setRouteMatchers(this.routeMatchers);
        dispatcherHandler.setInterceptorChains(this.interceptorChain);
        dispatcherHandler.setArgumentResolvers(this.argumentResolvers);
        dispatcherHandler.setReturnValueProcessors(this.returnValueProcessors);
        dispatcherHandler.setExceptionHandlers(this.exceptionHandlers);
        Mapping.from(prefix).whenNotNull(dispatcherHandler::setPrefix);
        Mapping.from(suffix).whenNotNull(dispatcherHandler::setSuffix);
        return dispatcherHandler;
    }
}
