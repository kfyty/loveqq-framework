package com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.web.server;

import com.kfyty.loveqq.framework.boot.template.thymeleaf.autoconfig.ThymeleafProperties;
import com.kfyty.loveqq.framework.core.autoconfig.BeanCustomizer;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnWebApplication;
import com.kfyty.loveqq.framework.web.mvc.netty.DispatcherHandler;
import org.thymeleaf.web.IWebApplication;

/**
 * 描述: thymeleaf 自动配置
 *
 * @author kfyty725
 * @date 2024/6/05 18:55
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnWebApplication(ConditionalOnWebApplication.WebApplicationType.SERVER)
public class NettyServerApplicationThymeleafAutoConfiguration {

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

    @Bean(resolveNested = false, ignoredAutowired = true)
    public IWebApplication servletWebApplication() {
        return new NettyServerWebApplication();
    }
}
