package com.kfyty.web.mvc.core.autoconfig;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ContextAfterRefreshed;
import com.kfyty.core.autoconfig.annotation.Bean;
import com.kfyty.core.autoconfig.annotation.Configuration;
import com.kfyty.core.autoconfig.annotation.Import;
import com.kfyty.core.autoconfig.condition.annotation.ConditionalOnBean;
import com.kfyty.web.mvc.core.WebServer;
import com.kfyty.web.mvc.core.annotation.Controller;
import com.kfyty.web.mvc.core.handler.DefaultRequestMappingMatcher;
import com.kfyty.web.mvc.core.handler.RequestMappingAnnotationHandler;
import com.kfyty.web.mvc.core.handler.RequestMappingHandler;
import com.kfyty.web.mvc.core.handler.RequestMappingMatcher;
import com.kfyty.web.mvc.core.mapping.MethodMapping;
import com.kfyty.web.mvc.core.processor.ControllerAdviceBeanPostProcessor;

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
    public ControllerAdviceBeanPostProcessor controllerAdviceBeanPostProcessor() {
        return new ControllerAdviceBeanPostProcessor();
    }

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
