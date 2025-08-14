package com.kfyty.loveqq.framework.web.mvc.servlet.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.lang.util.Mapping;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.core.handler.ExceptionHandler;
import com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.route.RouteMatcher;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;

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
@ComponentScan(includeFilter = @ComponentFilter(annotations = {WebFilter.class, WebListener.class, WebServlet.class}))
public class WebServletMvcAutoConfig {
    @Autowired
    private List<RouteMatcher> routeMatchers;

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Autowired(required = false)
    private List<ExceptionHandler> exceptionHandlers;

    @Bean(resolveNested = false, independent = true)
    public MultipartConfigElement multipartConfig(WebServerProperties serverProperties,
                                                  @Value("${k.mvc.multipart.maxRequestSize:-1}") int maxRequestSize,
                                                  @Value("${k.mvc.multipart.fileSizeThreshold:0}") int fileSizeThreshold) {
        Long maxSize = serverProperties.getMaxFileSize() == null ? Long.valueOf(-1L) : serverProperties.getMaxFileSize();
        return new MultipartConfigElement(serverProperties.getLocation(), maxSize, maxRequestSize, fileSizeThreshold);
    }

    @Bean(resolveNested = false, independent = true)
    public ServletContext servletContext(ServletWebServer webServer) {
        return webServer.getServletContext();
    }

    @Bean
    public DispatcherServlet dispatcherServlet(@Value("${k.server.view.prefix:}") String prefix,
                                               @Value("${k.server.view.suffix:.jsp}") String suffix) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setRouteMatchers(this.routeMatchers);
        dispatcherServlet.setInterceptorChains(this.interceptorChain);
        dispatcherServlet.setArgumentResolvers(this.argumentResolvers);
        dispatcherServlet.setReturnValueProcessors(this.returnValueProcessors);
        dispatcherServlet.setExceptionHandlers(this.exceptionHandlers);
        Mapping.from(prefix).whenNotNull(dispatcherServlet::setPrefix);
        Mapping.from(suffix).whenNotNull(dispatcherServlet::setSuffix);
        return dispatcherServlet;
    }
}
