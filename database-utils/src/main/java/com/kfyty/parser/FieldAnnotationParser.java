package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.AutoWired;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Set;

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

    public void parseFieldAnnotation(Set<Class<?>> classSet) throws Exception {
        if(CommonUtil.empty(classSet)) {
            return;
        }
        for (Class<?> clazz : classSet) {
            if(clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Component.class)) {
                this.parseAutoConfiguration(clazz);
            }
        }
    }

    private void parseAutoConfiguration(Class<?> clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        Object o = this.applicationConfigurable.getBeanResources().get(clazz);
        if(CommonUtil.empty(fields)) {
            return;
        }
        if(o == null) {
            log.error(": not found bean resources: [{}] !", clazz);
            return;
        }
        for (Field field : fields) {
            this.parseAutoWriedAnnotation(o, field);
        }
    }

    private void parseAutoWriedAnnotation(Object o, Field field) throws Exception {
        if(!field.isAnnotationPresent(AutoWired.class)) {
            return;
        }
        Object value = this.applicationConfigurable.getBeanResources().get(field.getType());
        if(value == null) {
            log.error(": not found bean resources: [{}] !", field.getType());
            return;
        }
        field.setAccessible(true);
        field.set(o, value);
        if(log.isDebugEnabled()) {
            log.debug(": found auto wired: [{}] !", field.getType());
        }
    }
}
