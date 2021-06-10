package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.request.resolver.HandlerMethodArgumentResolver;
import com.kfyty.mvc.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.servlet.HandlerInterceptor;
import com.kfyty.mvc.tomcat.TomcatConfig;
import com.kfyty.mvc.tomcat.TomcatWebServer;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.ConfigurableContext;
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

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:53
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = WebSocketAutoConfig.class)
public class TomcatAutoConfig implements BeanRefreshComplete, DestroyBean {
    @Autowired
    private ConfigurableContext configurableContext;

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorChain;

    @Autowired(required = false)
    private List<HandlerMethodArgumentResolver> argumentResolvers;

    @Autowired(required = false)
    private List<HandlerMethodReturnValueProcessor> returnValueProcessors;

    @Bean
    public TomcatConfig tomcatConfig() {
        TomcatConfig config = new TomcatConfig(configurableContext.getPrimarySource());
        configurableContext.getBeanWithAnnotation(WebFilter.class).values().forEach(e -> config.addWebFilter((Filter) e));
        configurableContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
        return config;
    }

    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        if(this.interceptorChain != null) {
            dispatcherServlet.getInterceptorChains().addAll(this.interceptorChain);
        }
        if(this.argumentResolvers != null) {
            dispatcherServlet.getArgumentResolvers().addAll(this.argumentResolvers);
        }
        if(this.returnValueProcessors != null) {
            dispatcherServlet.getReturnValueProcessors().addAll(this.returnValueProcessors);
        }
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
    public void onComplete(Class<?> primarySource, String ... args) {
        WebServer server = configurableContext.getBean(WebServer.class);
        if(server != null) {
            server.start();
        }
    }

    @Override
    public void onDestroy() {
        WebServer server = configurableContext.getBean(WebServer.class);
        if(server != null) {
            server.stop();
        }
    }
}
