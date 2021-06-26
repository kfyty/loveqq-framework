package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.handler.MvcAnnotationHandler;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Import;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
@Import(config = ControllerAdviceBeanPostProcessor.class)
public class MvcAutoConfig implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Autowired
    private MvcAnnotationHandler mvcAnnotationHandler;

    @Bean
    public MvcAnnotationHandler mvcAnnotationHandler() {
        return new MvcAnnotationHandler();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.applicationContext = context;
    }

    @PostConstruct
    public void initMethodMapping() {
        Map<String, Object> controllers = applicationContext.getBeanWithAnnotation(Controller.class);
        controllers.putAll(applicationContext.getBeanWithAnnotation(RestController.class));
        for (Object value : controllers.values()) {
            mvcAnnotationHandler.setMappingController(value);
            mvcAnnotationHandler.buildURLMappingMap();
        }
    }
}
