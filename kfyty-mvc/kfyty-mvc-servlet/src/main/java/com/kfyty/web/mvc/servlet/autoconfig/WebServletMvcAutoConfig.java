package com.kfyty.web.mvc.servlet.autoconfig;

import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.web.mvc.core.handler.RequestMappingMatcher;
import com.kfyty.web.mvc.servlet.DispatcherServlet;
import com.kfyty.web.mvc.servlet.ServletWebServer;
import com.kfyty.web.mvc.servlet.interceptor.HandlerInterceptor;
import com.kfyty.web.mvc.servlet.request.resolver.ServletHandlerMethodArgumentResolver;
import com.kfyty.web.mvc.servlet.request.resolver.ServletHandlerMethodReturnValueProcessor;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;

import java.util.List;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(ServletWebServer.class)
@ComponentFilter(annotations = {WebFilter.class, WebListener.class})
public class WebServletMvcAutoConfig {
    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<ServletHandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<ServletHandlerMethodReturnValueProcessor> returnValueProcessors;

    @Bean
    public MultipartConfigElement multipartConfig(@Value("${k.mvc.multipart.location:}") String location,
                                                  @Value("${k.mvc.multipart.maxFileSize:-1}") int maxFileSize,
                                                  @Value("${k.mvc.multipart.maxRequestSize:-1}") int maxRequestSize,
                                                  @Value("${k.mvc.multipart.fileSizeThreshold:0}") int fileSizeThreshold) {
        return new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
    }

    @Bean
    public ServletContext servletContext(ServletWebServer webServer) {
        return webServer.getServletContext();
    }

    @Bean
    public DispatcherServlet dispatcherServlet(RequestMappingMatcher requestMappingMatcher) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        this.interceptorChain.forEach(dispatcherServlet::addInterceptor);
        this.argumentResolvers.forEach(dispatcherServlet::addArgumentResolver);
        this.returnValueProcessors.forEach(dispatcherServlet::addReturnProcessor);
        dispatcherServlet.setRequestMappingMatcher(requestMappingMatcher);
        return dispatcherServlet;
    }
}
