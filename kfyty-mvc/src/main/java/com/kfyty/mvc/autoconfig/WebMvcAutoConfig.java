package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.handler.RequestMappingAnnotationHandler;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.servlet.HandlerInterceptor;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnMissingBean;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = ControllerAdviceBeanPostProcessor.class)
public class WebMvcAutoConfig {
    @Autowired
    private ApplicationContext applicationContext;

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
        return dispatcherServlet;
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestMappingAnnotationHandler requestMappingAnnotationHandler() {
        return new RequestMappingAnnotationHandler();
    }

    @PostConstruct
    public void initMethodMapping() {
        RequestMappingAnnotationHandler requestMappingAnnotationHandler = this.requestMappingAnnotationHandler();
        for (Object value : this.applicationContext.getBeanWithAnnotation(Controller.class).values()) {
            requestMappingAnnotationHandler.doParseMappingController(value);
        }
    }
}
