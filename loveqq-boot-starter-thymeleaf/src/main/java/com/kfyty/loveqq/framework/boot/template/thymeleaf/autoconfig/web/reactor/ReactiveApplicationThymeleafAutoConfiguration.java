package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.reactor;

import com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.ThymeleafProperties;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import com.kfyty.loveqq.framework.web.mvc.reactor.DispatcherHandler;
import org.thymeleaf.web.IWebApplication;

/**
 * 描述: thymeleaf 自动配置
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnWebApplication(ConditionalOnWebApplication.WebApplicationType.REACTOR)
public class ReactiveApplicationThymeleafAutoConfiguration {

    @Bean
    public BeanCustomizer<DispatcherHandler> serverThymeleafViewPrefixSufixBeanCustomizer(ThymeleafProperties thymeleafProperties) {
        return e -> {
            e.setPrefix(thymeleafProperties.getPrefix());
            e.setSuffix(thymeleafProperties.getSuffix());
        };
    }

    @Bean
    public ThymeleafViewHandlerMethodReturnValueProcessor serverThymeleafViewHandlerMethodReturnValueProcessor() {
        return new ThymeleafViewHandlerMethodReturnValueProcessor();
    }

    @Bean(resolveNested = false, independent = true)
    public IWebApplication servletWebApplication() {
        return new ReactiveWebApplication();
    }
}
