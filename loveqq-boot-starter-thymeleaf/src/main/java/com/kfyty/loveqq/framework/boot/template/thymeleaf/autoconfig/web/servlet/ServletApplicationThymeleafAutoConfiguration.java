package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.servlet;

import com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.ThymeleafProperties;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import com.kfyty.loveqq.framework.web.core.request.resolver.HandlerMethodReturnValueProcessor;
import com.kfyty.loveqq.framework.web.mvc.servlet.DispatcherServlet;
import jakarta.servlet.ServletContext;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

/**
 * 描述: thymeleaf 自动配置
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnWebApplication(ConditionalOnWebApplication.WebApplicationType.SERVLET)
public class ServletApplicationThymeleafAutoConfiguration {

    @Bean
    public BeanCustomizer<DispatcherServlet> servletThymeleafViewPrefixSufixBeanCustomizer(ThymeleafProperties thymeleafProperties) {
        return e -> {
            e.setPrefix(thymeleafProperties.getPrefix());
            e.setSuffix(thymeleafProperties.getSuffix());
        };
    }

    @Bean
    public HandlerMethodReturnValueProcessor servletThymeleafViewHandlerMethodReturnValueProcessor() {
        return new ThymeleafViewHandlerMethodReturnValueProcessor();
    }

    @Bean(resolveNested = false, ignoredAutowired = true)
    public IWebApplication servletWebApplication(ServletContext servletContext) {
        return JakartaServletWebApplication.buildApplication(servletContext);
    }
}
