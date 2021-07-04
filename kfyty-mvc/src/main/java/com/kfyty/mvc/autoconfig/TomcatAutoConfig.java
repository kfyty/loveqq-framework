package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.servlet.HandlerInterceptor;
import com.kfyty.mvc.tomcat.TomcatConfig;
import com.kfyty.mvc.tomcat.TomcatWebServer;
import com.kfyty.support.autoconfig.ContextRefreshCompleted;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Import;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:53
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = WebSocketAutoConfig.class)
public class TomcatAutoConfig implements ContextRefreshCompleted, DestroyBean {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Bean
    public TomcatConfig tomcatConfig() {
        TomcatConfig config = new TomcatConfig(applicationContext.getPrimarySource());
        applicationContext.getBeanWithAnnotation(WebFilter.class).values().forEach(e -> config.addWebFilter((Filter) e));
        applicationContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
        return config;
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        this.interceptorChain.forEach(dispatcherServlet::addInterceptor);
        this.argumentResolvers.forEach(dispatcherServlet::addArgumentResolver);
        this.returnValueProcessors.forEach(dispatcherServlet::addReturnProcessor);
        return dispatcherServlet;
    }

    @Bean
    public TomcatWebServer tomcatWebServer(TomcatConfig config, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(config, dispatcherServlet);
    }

    @Bean
    public ServletContext servletContext(TomcatWebServer webServer) {
        return webServer.getServletContext();
    }

    @Override
    public void onCompleted(ApplicationContext applicationContext) {
        WebServer server = this.applicationContext.getBean(WebServer.class);
        if(server != null) {
            server.start();
        }
    }

    @Override
    public void onDestroy() {
        Optional.ofNullable(this.applicationContext).map(e -> e.getBean(WebServer.class)).ifPresent(WebServer::stop);
    }
}
