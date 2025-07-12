package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnMissingBean;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnProperty;
import com.kfyty.loveqq.framework.core.lang.Lazy;
import com.kfyty.loveqq.framework.web.core.WebServer;
import com.kfyty.loveqq.framework.web.core.annotation.Controller;
import com.kfyty.loveqq.framework.web.core.cors.CorsConfiguration;
import com.kfyty.loveqq.framework.web.core.cors.CorsFilter;
import com.kfyty.loveqq.framework.web.core.filter.Filter;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingHandler;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.mapping.MethodMapping;

import java.util.List;
import java.util.Map;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@ConditionalOnBean(WebServer.class)
@Import(config = WebServerProperties.class)
public class WebMvcAutoConfig implements ContextAfterRefreshed {

    @Bean
    @ConfigurationProperties("k.mvc.cors")
    @ConditionalOnProperty(prefix = "k.mvc.cors", value = "allowOrigin", matchIfNonNull = true)
    public CorsConfiguration defaultCorsConfiguration() {
        return new CorsConfiguration();
    }

    @Bean
    @ConditionalOnBean(CorsConfiguration.class)
    @ConditionalOnMissingBean(CorsFilter.class)
    public Filter defaultCorsFilter(CorsConfiguration configuration) {
        return new CorsFilter(configuration);
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        RequestMappingHandler requestMappingHandler = applicationContext.getBean(RequestMappingHandler.class);
        RequestMappingMatcher requestMappingMatcher = applicationContext.getBean(RequestMappingMatcher.class);
        for (Map.Entry<String, BeanDefinition> entry : applicationContext.getBeanDefinitionWithAnnotation(Controller.class, true).entrySet()) {
            List<MethodMapping> methodMappings = requestMappingHandler.resolveRequestMapping(entry.getValue().getBeanType(), new Lazy<>(() -> applicationContext.getBean(entry.getKey())));
            requestMappingMatcher.registryMethodMapping(methodMappings);
        }
        this.startWebServer(applicationContext);
    }

    public void startWebServer(ApplicationContext applicationContext) {
        WebServer server = applicationContext.getBean(WebServer.class);
        if (server != null && !server.isStart()) {
            server.start();
        }
    }
}
