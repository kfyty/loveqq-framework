package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.tomcat.TomcatConfig;
import com.kfyty.mvc.tomcat.TomcatWebServer;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.DestroyBean;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.support.autoconfig.annotation.Import;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.support.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.support.event.ApplicationListener;
import com.kfyty.support.event.ContextRefreshedEvent;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;
import java.util.EventListener;
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
public class TomcatAutoConfig implements DestroyBean, ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(TomcatWebServer.class)
    @ConfigurationProperties("k.mvc.tomcat")
    public TomcatConfig tomcatConfig() {
        TomcatConfig config = new TomcatConfig(applicationContext.getPrimarySource());
        applicationContext.getBeanWithAnnotation(WebFilter.class).values().forEach(e -> config.addWebFilter((Filter) e));
        applicationContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
        return config;
    }

    @Bean
    @ConditionalOnMissingBean
    public TomcatWebServer tomcatWebServer(TomcatConfig config, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(config, dispatcherServlet);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServletContext servletContext(TomcatWebServer webServer) {
        return webServer.getServletContext();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        WebServer server = this.applicationContext.getBean(WebServer.class);
        if (server != null) {
            server.start();
        }
    }

    @Override
    public void onDestroy() {
        Optional.ofNullable(this.applicationContext)
                .map(e -> e.getBean(WebServer.class))
                .ifPresent(WebServer::stop);
    }
}
