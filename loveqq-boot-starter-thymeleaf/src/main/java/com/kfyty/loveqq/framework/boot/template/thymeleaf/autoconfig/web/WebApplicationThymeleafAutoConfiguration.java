package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web;

import com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.ThymeleafProperties;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.web.core.WebServer;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import com.kfyty.loveqq.framework.web.mvc.servlet.request.resolver.ServletHandlerMethodReturnValueProcessor;
import jakarta.servlet.ServletContext;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.servlet.IServletWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

/**
 * 描述: thymeleaf 自动配置
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean({WebServer.class, ServletContext.class})
public class WebApplicationThymeleafAutoConfiguration {

    @Bean
    public BeanCustomizer<DispatcherServlet> thymeleafViewPrefixSufixBeanCustomizer(ThymeleafProperties thymeleafProperties) {
        return e -> {
            e.setPrefix(thymeleafProperties.getPrefix());
            e.setSuffix(thymeleafProperties.getSuffix());
        };
    }

    @Bean
    public ServletHandlerMethodReturnValueProcessor thymeleafViewHandlerMethodReturnValueProcessor() {
        return new ThymeleafViewHandlerMethodReturnValueProcessor();
    }

    @Bean
    public IServletWebApplication servletWebApplication(ServletContext servletContext) {
        return JakartaServletWebApplication.buildApplication(servletContext);
    }

    @Bean
    public ITemplateResolver webApplicationTemplateResolver(ThymeleafProperties thymeleafProperties, IServletWebApplication webApplication) {
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setCacheable(thymeleafProperties.isCacheable());
        templateResolver.setTemplateMode(thymeleafProperties.getTemplateMode());
        templateResolver.setCharacterEncoding(thymeleafProperties.getCharacterEncoding());
        templateResolver.setPrefix(thymeleafProperties.getPrefix());
        templateResolver.setSuffix(thymeleafProperties.getSuffix());
        return templateResolver;
    }
}
