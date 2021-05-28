package com.kfyty.boot.resolver;

import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.jdbc.ReturnType;
import com.kfyty.util.CommonUtil;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class FieldAnnotationResolver {

    private final ApplicationContext applicationContext;

    public void doResolver(boolean init) {
        for (Map.Entry<Class<?>, BeanResources> entry : this.applicationContext.getBeanResources().entrySet()) {
            for (Map.Entry<String, Object> beanEntry : entry.getValue().getBeans().entrySet()) {
                this.doResolver(entry.getKey(), beanEntry.getValue(), init);
            }
        }
    }

    @SneakyThrows
    private void doResolver(Class<?> clazz, Object value, boolean init) {
        Field[] fields = clazz.getDeclaredFields();
        if(CommonUtil.empty(fields)) {
            return;
        }
        for (Field field : fields) {
            if(field.isAnnotationPresent(Autowired.class)) {
                this.processAutowiredAnnotation(value, field, field.getAnnotation(Autowired.class), init);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    private void processAutowiredAnnotation(Object o, Field field, Autowired autowired, boolean init) throws Exception {
        ReturnType fieldType = ReturnType.getReturnType(field.getGenericType(), field.getType());
        if(List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())) {
            this.processAutowiredAnnotation(o, field, fieldType.getFirstParameterizedType(), autowired, init);
        } else if(Map.class.isAssignableFrom(field.getType())) {
            this.processAutowiredAnnotation(o, field, fieldType.getSecondParameterizedType(), autowired, init);
        } else if(Class.class.isAssignableFrom(field.getType())) {
            this.processAutowiredAnnotation(o, field, fieldType.getFirstParameterizedType(), autowired, init);
        } else  {
            this.processAutowiredAnnotation(o, field, fieldType.getReturnType(), autowired, init);
        }
    }

    private void processAutowiredAnnotation(Object o, Field field, Class<?> fieldType, Autowired autowired, boolean init) throws Exception {
        Map<String, ?> beans = this.applicationContext.getBeanOfType(fieldType);
        if(beans.isEmpty()) {
            if(!init && autowired.required()) {
                throw new IllegalArgumentException("autowired failed for bean [" + o.getClass() + "], no bean found of type: " + fieldType);
            }
            return;
        }
        boolean isAccessible = field.isAccessible();
        field.setAccessible(true);
        if(List.class.isAssignableFrom(field.getType())) {
            field.set(o, new ArrayList<>(beans.values()));
        } else if(Set.class.isAssignableFrom(field.getType())) {
            field.set(o, new HashSet<>(beans.values()));
        } else if(Map.class.isAssignableFrom(field.getType())) {
            field.set(o, beans);
        } else {
            if(beans.size() == 1) {
                field.set(o, beans.values().iterator().next());
            } else {
                if(!beans.containsKey(autowired.value())) {
                    throw new IllegalArgumentException("autowired failed for bean [" + o.getClass() + "], more than one bean found of type: " + fieldType);
                }
                field.set(o, beans.get(autowired.value()));
            }
        }
        field.setAccessible(isAccessible);
        if(log.isDebugEnabled()) {
            log.debug(": autowired bean: [{}] to [{}] !", fieldType, o);
        }
    }
}
