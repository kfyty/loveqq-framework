package com.kfyty.mvc.autoconfig;

import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.handler.MvcAnnotationHandler;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.ConfigurableContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;

import java.util.Map;

/**
 * 描述: mvc 自动配置
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 */
@Configuration
public class MvcAutoConfig implements BeanRefreshComplete {
    @Autowired
    private ConfigurableContext configurableContext;

    @Autowired
    private MvcAnnotationHandler mvcAnnotationHandler;

    @Bean
    public MvcAnnotationHandler mvcAnnotationHandler() {
        return new MvcAnnotationHandler();
    }

    @Override
    public void onComplete(Class<?> primarySource, String ... args) {
        Map<String, Object> controllers = configurableContext.getBeanWithAnnotation(Controller.class);
        controllers.putAll(configurableContext.getBeanWithAnnotation(RestController.class));
        for (Object value : controllers.values()) {
            mvcAnnotationHandler.setMappingController(value);
            mvcAnnotationHandler.buildURLMappingMap();
        }
    }
}
