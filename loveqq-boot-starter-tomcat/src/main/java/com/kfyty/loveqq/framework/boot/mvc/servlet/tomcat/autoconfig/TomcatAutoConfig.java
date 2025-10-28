package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.TomcatWebServer;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnEnableWebMvc;
import com.kfyty.loveqq.framework.core.utils.BeanUtil;
import com.kfyty.loveqq.framework.web.core.RegistrationBean;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.FilterRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.filter.FilterAdapter;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.catalina.LifecycleListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:53
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnEnableWebMvc
public class TomcatAutoConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebServerProperties webServerProperties;

    @Autowired(required = false)
    private List<LifecycleListener> lifecycleListeners;

    @Autowired(required = false)
    private List<ServletContainerInitializer> servletContainerInitializers;

    @Autowired(required = false)
    private List<ServletContextListener> servletContextListeners;

    @Autowired(required = false)
    private List<ServletRequestListener> servletRequestListeners;

    @ConfigurationProperties("k.mvc.tomcat")
    @Bean(resolveNested = false, independent = true)
    public TomcatProperties tomcatProperties(MultipartConfigElement multipartConfig) {
        TomcatProperties config = this.webServerProperties.copy(new TomcatProperties(this.applicationContext.getPrimarySource(), multipartConfig));
        config.setLifecycleListeners(this.lifecycleListeners);
        config.setServletContainerInitializers(this.servletContainerInitializers);
        config.setServletContextListeners(this.servletContextListeners);
        config.setServletRequestListeners(this.servletRequestListeners);
        this.collectAndConfigListener(config);
        this.collectAndConfigFilter(config);
        this.collectAndConfigServlet(config);
        return config;
    }

    @Bean(destroyMethod = "stop", resolveNested = false, independent = true)
    public TomcatWebServer tomcatWebServer(TomcatProperties config, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(config, dispatcherServlet);
    }

    protected void collectAndConfigListener(TomcatProperties config) {
        this.applicationContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
    }

    protected void collectAndConfigFilter(TomcatProperties config) {
        List<Object> filters = this.collectBeans(Filter.class, FilterRegistrationBean.class);
        for (Object filter : filters) {
            if (filter instanceof FilterRegistrationBean) {
                config.addWebFilter((FilterRegistrationBean) filter);
            } else if (filter instanceof Filter) {
                config.addWebFilter((Filter) filter);
            } else {
                com.kfyty.loveqq.framework.web.core.filter.Filter _filter_ = (com.kfyty.loveqq.framework.web.core.filter.Filter) filter;
                RegistrationBean<FilterRegistrationBean> registrationBean = new FilterRegistrationBean()
                        .setFilter(new FilterAdapter(_filter_))
                        .setFilterName(filter.getClass().getName())
                        .setDisplayName(filter.getClass().getName())
                        .setUrlPatterns(Arrays.asList(_filter_.getPattern()));
                config.addWebFilter((FilterRegistrationBean) registrationBean);
            }
        }
    }

    protected void collectAndConfigServlet(TomcatProperties config) {
        List<Object> servlets = this.collectBeans(Servlet.class, ServletRegistrationBean.class);
        for (Object servlet : servlets) {
            if (servlet instanceof ServletRegistrationBean) {
                config.addWebServlet((ServletRegistrationBean) servlet);
            } else {
                config.addWebServlet((Servlet) servlet);
            }
        }
    }

    protected List<Object> collectBeans(Class<?> filterClass, Class<? extends RegistrationBean<?>> clazz) {
        List<Object> beans = new ArrayList<>();
        Collection<?> beansWithAnnotation = this.applicationContext.getBeanOfType(filterClass).values();
        Collection<?> beansWithRegistration = this.applicationContext.getBeanOfType(clazz).values();

        beans.addAll(beansWithAnnotation);
        beans.addAll(beansWithRegistration);

        if (filterClass == Filter.class) {
            beans.addAll(this.applicationContext.getBeanOfType(com.kfyty.loveqq.framework.web.core.filter.Filter.class).values());
        }

        if (beans.isEmpty()) {
            return beans;
        }

        return beans.stream().sorted(Comparator.comparing(BeanUtil::getBeanOrder)).collect(Collectors.toList());
    }
}
