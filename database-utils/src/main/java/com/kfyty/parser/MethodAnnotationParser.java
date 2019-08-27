package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.Bean;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.util.CommonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 功能描述: 方法注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 15:17
 * @since JDK 1.8
 */
@Slf4j
@AllArgsConstructor
public class MethodAnnotationParser {
    private ApplicationConfigurable applicationConfigurable;

    public void parseMethodAnnotation(Set<Class<?>> classSet) throws Exception {
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
        Method[] methods = clazz.getDeclaredMethods();
        Object o = this.applicationConfigurable.getBeanResources().get(clazz);
        if(CommonUtil.empty(methods)) {
            return;
        }
        if(o == null) {
            log.error(": not found bean resources: [{}] !", clazz);
            return;
        }
        for (Method method : methods) {
            this.parseBeanAnnotation(o, method);
        }
    }

    private void parseBeanAnnotation(Object o, Method method) throws Exception {
        if(!method.isAnnotationPresent(Bean.class)) {
            return;
        }
        this.applicationConfigurable.getBeanResources().put(method.getReturnType(), method.invoke(o));
        if(log.isDebugEnabled()) {
            log.debug(": found bean resource: [{}] !", method.getReturnType());
        }
    }
}
