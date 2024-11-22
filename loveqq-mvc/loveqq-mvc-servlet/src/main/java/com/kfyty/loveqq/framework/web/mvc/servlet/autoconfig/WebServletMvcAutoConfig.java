package com.kfyty.loveqq.framework.web.mvc.servlet.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentFilter;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ComponentScan;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Value;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.web.core.cors.CorsConfiguration;
import com.kfyty.loveqq.framework.web.core.handler.ExceptionHandler;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.interceptor.HandlerInterceptor;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletWebServer;
import com.kfyty.loveqq.framework.web.mvc.servlet.filter.cors.CorsFilter;
import jakarta.servlet.Filter;
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
@ComponentScan(includeFilter = @ComponentFilter(annotations = {WebFilter.class, WebListener.class}))
public class WebServletMvcAutoConfig {
    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Autowired(required = false)
    private List<ExceptionHandler> exceptionHandlers;

    @Bean(resolveNested = false, independent = true)
    public MultipartConfigElement multipartConfig(@Value("${k.mvc.multipart.location:}") String location,
                                                  @Value("${k.mvc.multipart.maxFileSize:-1}") int maxFileSize,
                                                  @Value("${k.mvc.multipart.maxRequestSize:-1}") int maxRequestSize,
                                                  @Value("${k.mvc.multipart.fileSizeThreshold:0}") int fileSizeThreshold) {
        return new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
    }

    @Bean
    @ConditionalOnBean(CorsConfiguration.class)
    @ConditionalOnMissingBean(CorsFilter.class)
    public Filter defaultCorsFilter(CorsConfiguration configuration) {
        return new CorsFilter(configuration);
    }

    @Bean(resolveNested = false, independent = true)
    public ServletContext servletContext(ServletWebServer webServer) {
        return webServer.getServletContext();
    }

    @Bean
    public DispatcherServlet dispatcherServlet(RequestMappingMatcher requestMappingMatcher,
                                               @Value("${k.server.view.prefix:}") String prefix,
                                               @Value("${k.server.view.suffix:.jsp}") String suffix) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        dispatcherServlet.setInterceptorChains(this.interceptorChain);
        dispatcherServlet.setArgumentResolvers(this.argumentResolvers);
        dispatcherServlet.setReturnValueProcessors(this.returnValueProcessors);
        dispatcherServlet.setExceptionHandlers(this.exceptionHandlers);
        dispatcherServlet.setRequestMappingMatcher(requestMappingMatcher);
        dispatcherServlet.setPrefix(prefix);
        dispatcherServlet.setSuffix(suffix);
        return dispatcherServlet;
    }
}
