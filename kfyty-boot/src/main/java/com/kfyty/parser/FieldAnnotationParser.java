package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.AutoWired;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Map;

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

    public void parseFieldAnnotation() throws Exception {
        for (Map.Entry<Class<?>, Object> entry : this.applicationConfigurable.getBeanResources().entrySet()) {
            if(entry.getKey().isAnnotationPresent(Configuration.class) || entry.getKey().isAnnotationPresent(Component.class)) {
                this.parseAutoConfiguration(entry.getKey(), entry.getValue());
            }
        }
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
        field.setAccessible(true);
        field.set(o, value);
        if(log.isDebugEnabled()) {
            log.debug(": found auto wired: [{}] !", field.getType());
        }
    }
}
