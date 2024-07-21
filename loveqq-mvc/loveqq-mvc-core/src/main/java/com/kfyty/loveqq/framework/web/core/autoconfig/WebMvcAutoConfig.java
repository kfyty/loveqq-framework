package com.kfyty.loveqq.framework.web.core.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Bean;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Configuration;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;
import com.kfyty.loveqq.framework.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.loveqq.framework.web.core.WebServer;
import com.kfyty.loveqq.framework.web.core.annotation.Controller;
import com.kfyty.loveqq.framework.web.core.handler.DefaultRequestMappingMatcher;
import com.kfyty.loveqq.framework.web.core.handler.RequestMappingAnnotationHandler;
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
    public RequestMappingHandler requestMappingHandler() {
        return new RequestMappingAnnotationHandler();
    }

    @Bean
    public RequestMappingMatcher requestMappingMatcher() {
        return new DefaultRequestMappingMatcher();
    }

    @Override
    public void onAfterRefreshed(ApplicationContext applicationContext) {
        RequestMappingHandler requestMappingHandler = this.requestMappingHandler();
        RequestMappingMatcher requestMappingMatcher = this.requestMappingMatcher();
        for (Map.Entry<String, Object> entry : applicationContext.getBeanWithAnnotation(Controller.class).entrySet()) {
            if (applicationContext.getBeanDefinition(entry.getKey()).isAutowireCandidate()) {
                List<MethodMapping> methodMappings = requestMappingHandler.resolveRequestMapping(entry.getValue());
                requestMappingMatcher.registryMethodMapping(methodMappings);
            }
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
