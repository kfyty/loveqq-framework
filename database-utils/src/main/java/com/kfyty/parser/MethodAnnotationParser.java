package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.Bean;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.util.CommonUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述: 方法注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 15:17
 * @since JDK 1.8
 */
@Slf4j
public class MethodAnnotationParser {

    private Map<Class<?>, Object> beanMap;

    private ApplicationConfigurable applicationConfigurable;

    public MethodAnnotationParser(ApplicationConfigurable applicationConfigurable) {
        this.beanMap = new HashMap<>();
        this.applicationConfigurable = applicationConfigurable;
    }

    public void parseMethodAnnotation() throws Exception {
        this.beanMap.clear();
        for (Map.Entry<Class<?>, Object> entry : this.applicationConfigurable.getBeanResources().entrySet()) {
            if(entry.getKey().isAnnotationPresent(Configuration.class) || entry.getKey().isAnnotationPresent(Component.class)) {
                this.parseAutoConfiguration(entry.getKey(), entry.getValue());
            }
        }
        this.applicationConfigurable.getBeanResources().putAll(beanMap);
    }

    private void parseAutoConfiguration(Class<?> clazz, Object value) throws Exception {
        Method[] methods = clazz.getDeclaredMethods();
        if(CommonUtil.empty(methods)) {
            return;
        }
        for (Method method : methods) {
            if(method.isAnnotationPresent(Bean.class)) {
                this.parseBeanAnnotation(value, method);
            }
        }
    }

    private void parseBeanAnnotation(Object o, Method method) throws Exception {
        this.beanMap.put(method.getReturnType(), method.invoke(o));
        if(log.isDebugEnabled()) {
            log.debug(": found bean resource: [{}] !", method.getReturnType());
        }
    }
}
