package com.kfyty.boot.resolver;

import com.kfyty.boot.K;
import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.BeanDefine;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Qualifier;
import com.kfyty.util.CommonUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 功能描述: 方法注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/27 15:17
 * @since JDK 1.8
 */
@Slf4j
public class MethodAnnotationResolver {
    private final AnnotationConfigResolver configResolver;
    private final FieldAnnotationResolver fieldAnnotationResolver;
    private final ApplicationContext applicationContext;

    public MethodAnnotationResolver(AnnotationConfigResolver configResolver) {
        this.configResolver = configResolver;
        this.fieldAnnotationResolver = configResolver.getFieldAnnotationResolver();
        this.applicationContext = configResolver.getApplicationContext();
    }

    public void doResolver() {
        HashMap<Class<?>, BeanResources> beanResources = new HashMap<>(applicationContext.getBeanResources());
        for (Map.Entry<Class<?>, BeanResources> entry : beanResources.entrySet()) {
            for (Map.Entry<String, Object> beanEntry : entry.getValue().getBeans().entrySet()) {
                this.doResolver(beanEntry.getValue());
            }
        }
    }

    public void doResolver(Object bean) {
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if(method.isAnnotationPresent(Bean.class)) {
                this.processBeanAnnotation(bean, method, method.getAnnotation(Bean.class));
            }
        }
    }

    private void processBeanAnnotation(Object o, Method method, Bean bean) {
        processBeanAnnotation(new HashSet<>(2), o, method, bean);
    }

    @SneakyThrows
    private Object processBeanAnnotation(Set<Class<?>> resolving, Object o, Method method, Bean bean) {
        if(K.isExclude(method.getReturnType())) {
            log.info("exclude bean class: {}", method.getReturnType());
            return null;
        }
        Object obj = this.resolveBean(method, bean);
        if(obj != null) {
            return obj;
        }
        obj = method.invoke(o, this.resolveAutowiredBean(resolving, o, method));
        if(CommonUtil.empty(bean.value())) {
            applicationContext.registerBean(method.getReturnType(), obj);
        } else {
            applicationContext.registerBean(bean.value(), method.getReturnType(), obj);
        }
        this.fieldAnnotationResolver.doResolver(method.getReturnType(), obj, true);
        this.doResolver(obj);
        if(log.isDebugEnabled()) {
            log.debug(": instantiate bean resource: [{}] !", method.getReturnType());
        }
        return obj;
    }

    private Object[] resolveAutowiredBean(Set<Class<?>> resolving, Object source, Method method) {
        int index = 0;
        Object[] beans = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            if(resolving.contains(parameter.getType())) {
                throw new IllegalArgumentException("bean circular dependency: " + method.getReturnType() + " -> " + parameter.getType());
            }
            beans[index++] = this.resolveAutowiredBean(resolving, source, parameter);
        }
        return beans;
    }

    private Object resolveAutowiredBean(Set<Class<?>> resolving, Object source, Parameter parameter) {
        Class<?> target = parameter.getType();
        Object bean = this.resolveBean(parameter);
        if(bean != null) {
            return bean;
        }
        resolving.add(target);
        for (BeanDefine beanDefine : configResolver.getBeanDefines()) {
            if(!beanDefine.isInstance() && beanDefine.getBeanType().equals(target)) {
                return this.applicationContext.registerBean(beanDefine);
            }
        }
        for (Map.Entry<Class<?>, BeanResources> entry : this.applicationContext.getBeanResources().entrySet()) {
            for (Map.Entry<String, Object> beanEntry : entry.getValue().getBeans().entrySet()) {
                for (Method method : beanEntry.getValue().getClass().getMethods()) {
                    if(method.isAnnotationPresent(Bean.class) && method.getReturnType().equals(target)) {
                        return this.processBeanAnnotation(resolving, beanEntry.getValue(), method, method.getAnnotation(Bean.class));
                    }
                }
            }
        }
        throw new IllegalArgumentException("process bean annotation failed on object[" + source + "], no bean found of type: " + target);
    }

    private Object resolveBean(Method method, Bean bean) {
        if(!CommonUtil.empty(bean.value())) {
            return applicationContext.getBean(bean.value());
        }
        return applicationContext.getBean(CommonUtil.convert2BeanName(method.getReturnType().getSimpleName()));
    }

    private Object resolveBean(Parameter parameter) {
        if(parameter.isAnnotationPresent(Qualifier.class)) {
            return applicationContext.getBean(parameter.getAnnotation(Qualifier.class).value());
        }
        return applicationContext.getBean(parameter.getType());
    }
}
