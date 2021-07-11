package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.handler.RequestMappingAnnotationHandler;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Import;

import javax.annotation.PostConstruct;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = ControllerAdviceBeanPostProcessor.class)
public class WebMvcAutoConfig implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private RequestMappingAnnotationHandler requestMappingAnnotationHandler;

    @Bean
    public RequestMappingAnnotationHandler requestMappingAnnotationHandler() {
        return new RequestMappingAnnotationHandler();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    @PostConstruct
    public void initMethodMapping() {
        for (Object value : applicationContext.getBeanWithAnnotation(Controller.class).values()) {
            requestMappingAnnotationHandler.doParseMappingController(value);
        }
    }
}
