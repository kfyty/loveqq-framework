package com.kfyty.boot.mvc.servlet.tomcat.autoconfig;

import com.kfyty.boot.mvc.servlet.tomcat.TomcatWebServer;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.core.autoconfig.beans.BeanFactory;
import com.kfyty.core.autoconfig.condition.Condition;
import com.kfyty.core.autoconfig.condition.ConditionContext;
import com.kfyty.core.autoconfig.condition.annotation.Conditional;
import com.kfyty.core.support.AnnotationMetadata;
import com.kfyty.core.utils.AnnotationUtil;
import com.kfyty.web.mvc.core.autoconfig.WebServerProperties;
import com.kfyty.web.mvc.core.autoconfig.annotation.EnableWebMvc;
import com.kfyty.web.mvc.servlet.DispatcherServlet;
import com.kfyty.web.mvc.servlet.filter.FilterRegistrationBean;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
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
        this.applicationContext.getBeanOfType(FilterRegistrationBean.class).values().forEach(config::addWebFilter);
        this.applicationContext.getBeanWithAnnotation(WebFilter.class).values().forEach(e -> config.addWebFilter((Filter) e));
        this.applicationContext.getBeanWithAnnotation(WebListener.class).values().forEach(e -> config.addWebListener((EventListener) e));
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
