package com.kfyty.mvc.autoconfig;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.handler.DefaultRequestMappingMatcher;
import com.kfyty.mvc.handler.RequestMappingAnnotationHandler;
import com.kfyty.mvc.handler.RequestMappingHandler;
import com.kfyty.mvc.handler.RequestMappingMatcher;
import com.kfyty.mvc.mapping.MethodMapping;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = ControllerAdviceBeanPostProcessor.class)
public class WebMvcAutoConfig implements ContextAfterRefreshed {
    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Bean
    @ConditionalOnMissingBean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        this.interceptorChain.forEach(dispatcherServlet::addInterceptor);
        this.argumentResolvers.forEach(dispatcherServlet::addArgumentResolver);
        this.returnValueProcessors.forEach(dispatcherServlet::addReturnProcessor);
        dispatcherServlet.setRequestMappingMatcher(this.requestMappingMatcher());
        return dispatcherServlet;
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestMappingHandler requestMappingHandler() {
        return new RequestMappingAnnotationHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestMappingMatcher requestMappingMatcher() {
        return new DefaultRequestMappingMatcher();
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        RequestMappingHandler requestMappingHandler = this.requestMappingHandler();
        RequestMappingMatcher requestMappingMatcher = this.requestMappingMatcher();
        for (Map.Entry<String, Object> entry : applicationContext.getBeanWithAnnotation(Controller.class).entrySet()) {
            if (applicationContext.getBeanDefinition(entry.getKey()).isAutowireCandidate()) {
                List<MethodMapping> methodMappings = requestMappingHandler.resolveRequestMapping(entry.getValue());
                requestMappingMatcher.registryMethodMapping(methodMappings);
            }
        }
    }
}
