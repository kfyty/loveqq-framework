package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.AutoWired;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.Repository;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.annotation.Service;
import com.kfyty.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 功能描述: 属性注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Slf4j
@AllArgsConstructor
public class FieldAnnotationParser {

    private ApplicationConfigurable applicationConfigurable;

    public void parseFieldAnnotation() {
        this.applicationConfigurable.getBeanResources().entrySet().stream()
                .filter(e ->
                        e.getKey().isAnnotationPresent(Configuration.class)      ||
                        e.getKey().isAnnotationPresent(Component.class)          ||
                        e.getKey().isAnnotationPresent(Controller.class)         ||
                        e.getKey().isAnnotationPresent(RestController.class)     ||
                        e.getKey().isAnnotationPresent(Service.class)            ||
                        e.getKey().isAnnotationPresent(Repository.class))
                .forEach(e -> {
                    try {
                        this.parseAutoConfiguration(e.getKey(), e.getValue());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    private void parseAutoConfiguration(Class<?> clazz, Object value) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        if(CommonUtil.empty(fields)) {
            return;
        }
        for (Field field : fields) {
            if(field.isAnnotationPresent(AutoWired.class)) {
                this.parseAutoWriedAnnotation(value, field);
            }
        }
    }

    private void parseAutoWriedAnnotation(Object o, Field field) throws Exception {
        Object value = this.applicationConfigurable.getBeanResources().get(field.getType());
        if(value == null) {
            log.error(": not found bean resources: [{}] !", field.getType());
            return;
        }
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(o, value);
        field.setAccessible(isAccessible);
        if(log.isDebugEnabled()) {
            log.debug(": found auto wired: [{}] !", field.getType());
        }
    }
}
