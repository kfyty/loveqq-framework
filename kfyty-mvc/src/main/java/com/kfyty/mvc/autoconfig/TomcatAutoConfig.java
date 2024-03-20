package com.kfyty.mvc.autoconfig;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.core.autoconfig.annotation.Value;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.core.autoconfig.env.PropertyContext;
import com.kfyty.mvc.WebServer;
import com.kfyty.mvc.servlet.DispatcherServlet;
import com.kfyty.mvc.tomcat.TomcatConfig;
import com.kfyty.mvc.tomcat.TomcatWebServer;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;

import java.util.EventListener;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:53
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = WebSocketAutoConfig.class)
public class TomcatAutoConfig implements ContextAfterRefreshed {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PropertyContext propertyContext;

    @Bean
    public MultipartConfigElement multipartConfig(@Value("${k.mvc.multipart.location:}") String location,
                                                  @Value("${k.mvc.multipart.maxFileSize:-1}") int maxFileSize,
                                                  @Value("${k.mvc.multipart.maxRequestSize:-1}") int maxRequestSize,
                                                  @Value("${k.mvc.multipart.fileSizeThreshold:0}") int fileSizeThreshold) {
        return new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties("k.mvc.tomcat")
    public TomcatConfig tomcatConfig(MultipartConfigElement multipartConfig) {
        TomcatConfig config = new TomcatConfig(this.applicationContext.getPrimarySource(), multipartConfig);
        if (this.propertyContext.contains("k.server.port")) {
            config.setPort(Integer.parseInt(this.propertyContext.getProperty("k.server.port")));
        }
        this.applicationContext.getBeanWithAnnotation(WebFilter.class).values().forEach(e -> config.addWebFilter((Filter) e));
        this.applicationContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
        return config;
    }

    @Bean(destroyMethod = "stop")
    public TomcatWebServer tomcatWebServer(TomcatConfig config, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(config, dispatcherServlet);
    }

    @Bean
    public ServletContext servletContext(TomcatWebServer webServer) {
        return webServer.getServletContext();
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        WebServer server = applicationContext.getBean(WebServer.class);
        if (server != null && !server.isStart()) {
            server.start();
        }
    }
}
