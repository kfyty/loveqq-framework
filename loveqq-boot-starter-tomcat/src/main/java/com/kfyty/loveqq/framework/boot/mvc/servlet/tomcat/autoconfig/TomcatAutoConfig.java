package com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.autoconfig;

import com.kfyty.loveqq.framework.boot.mvc.servlet.tomcat.TomcatWebServer;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Autowired;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory;
import com.kfyty.loveqq.framework.core.autoconfig.condition.Condition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.ConditionContext;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.loveqq.framework.core.support.AnnotationMetadata;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.web.core.autoconfig.WebServerProperties;
import com.kfyty.loveqq.framework.web.core.autoconfig.annotation.EnableWebMvc;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.ServletRegistrationBean;
import com.kfyty.loveqq.framework.web.mvc.servlet.filter.FilterRegistrationBean;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;

import java.util.EventListener;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/5/28 14:53
 * @email kfyty725@hotmail.com
 */
@Configuration
@Conditional(TomcatAutoConfig.TomcatWebServerAutoConfigCondition.class)
public class TomcatAutoConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private WebServerProperties webServerProperties;

    @Bean
    @ConfigurationProperties("k.mvc.tomcat")
    public TomcatProperties tomcatProperties(MultipartConfigElement multipartConfig) {
        TomcatProperties config = new TomcatProperties(this.applicationContext.getPrimarySource(), multipartConfig);
        config.setPort(this.webServerProperties.getPort());
        this.applicationContext.getBeanWithAnnotation(WebServlet.class).values().forEach(e -> config.addWebServlet((Servlet) e));
        this.applicationContext.getBeanWithAnnotation(WebFilter.class).values().forEach(e -> config.addWebFilter((Filter) e));
        this.applicationContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
        this.applicationContext.getBeanOfType(ServletRegistrationBean.class).values().forEach(config::addWebServlet);
        this.applicationContext.getBeanOfType(FilterRegistrationBean.class).values().forEach(config::addWebFilter);
        return config;
    }

    @Bean(destroyMethod = "stop")
    public TomcatWebServer tomcatWebServer(TomcatProperties config, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(config, dispatcherServlet);
    }

    static class TomcatWebServerAutoConfigCondition implements Condition {

        @Override
        public boolean isMatch(ConditionContext context, AnnotationMetadata<?> metadata) {
            BeanFactory beanFactory = context.getBeanFactory();
            if (beanFactory instanceof ConfigurableApplicationContext) {
                Class<?> primarySource = ((ConfigurableApplicationContext) beanFactory).getPrimarySource();
                return AnnotationUtil.hasAnnotation(primarySource, EnableWebMvc.class);
            }
            return false;
        }
    }
}
