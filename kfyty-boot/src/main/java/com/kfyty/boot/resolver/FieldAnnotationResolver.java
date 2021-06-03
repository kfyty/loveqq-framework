package com.kfyty.boot.resolver;

import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.support.utils.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述: 属性注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 10:43
 * @since JDK 1.8
 */
@Slf4j
public class FieldAnnotationResolver {
    private final AnnotationConfigResolver configResolver;
    private final ApplicationContext applicationContext;

    public FieldAnnotationResolver(AnnotationConfigResolver configResolver) {
        this.configResolver = configResolver;
        this.applicationContext = configResolver.getApplicationContext();
    }

    public void doResolver(boolean init) {
        for (Map.Entry<Class<?>, BeanResources> entry : this.applicationContext.getBeanResources().entrySet()) {
            for (Map.Entry<String, Object> beanEntry : entry.getValue().getBeans().entrySet()) {
                this.doResolver(entry.getKey(), beanEntry.getValue(), init);
            }
        }
    }

    public void doResolver(Class<?> clazz, Object value, boolean init) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(field.isAnnotationPresent(Autowired.class)) {
                this.processAutowiredAnnotation(value, field, field.getAnnotation(Autowired.class), init);
            }
        }
    }

    @SneakyThrows
    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    private void processAutowiredAnnotation(Object o, Field field, Autowired autowired, boolean init) {
        ReturnType fieldType = ReturnType.getReturnType(field.getGenericType(), field.getType());
        if(List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())) {
            this.processAutowiredAnnotation(o, field, fieldType.getFirstParameterizedType(), autowired, init);
            return;
        }
        if(Map.class.isAssignableFrom(field.getType())) {
            this.processAutowiredAnnotation(o, field, fieldType.getSecondParameterizedType(), autowired, init);
            return;
        }
        if(Class.class.isAssignableFrom(field.getType())) {
            this.processAutowiredAnnotation(o, field, fieldType.getFirstParameterizedType(), autowired, init);
            return;
        }
        this.processAutowiredAnnotation(o, field, fieldType.getReturnType(), autowired, init);
    }

    private void processAutowiredAnnotation(Object o, Field field, Class<?> fieldType, Autowired autowired, boolean init) throws Exception {
        Map<String, ?> beans = this.applicationContext.getBeanOfType(fieldType);
        if(beans.isEmpty()) {
            if(!init && autowired.required()) {
                throw new IllegalArgumentException("autowired failed for bean [" + o.getClass() + "], no bean found of type: " + fieldType);
            }
            return;
        }
        Object value = null;
        if(List.class.isAssignableFrom(field.getType())) {
            value = new ArrayList<>(beans.values());
        }
        if(Set.class.isAssignableFrom(field.getType())) {
            value = new HashSet<>(beans.values());
        }
        if(Map.class.isAssignableFrom(field.getType())) {
            value = beans;
        }
        if(value == null) {
            if(beans.size() == 1) {
                value = beans.values().iterator().next();
            } else {
                value = beans.get(autowired.value());
                if(value == null) {
                    throw new IllegalArgumentException("autowired failed for bean [" + o.getClass() + "], more than one bean found of type: " + fieldType);
                }
            }
        }
        ReflectUtil.setFieldValue(o, field, value);
        if(log.isDebugEnabled()) {
            log.debug(": autowired bean: [{}] to [{}] !", fieldType, o);
        }
    }
}
