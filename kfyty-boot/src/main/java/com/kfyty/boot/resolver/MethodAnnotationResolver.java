package com.kfyty.boot.resolver;

import com.kfyty.boot.K;
import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.util.CommonUtil;
import lombok.SneakyThrows;
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
public class MethodAnnotationResolver {

    private final Map<Class<?>, BeanResources> beanMap;

    private final ApplicationContext applicationContext;

    public MethodAnnotationResolver(ApplicationContext applicationContext) {
        this.beanMap = new HashMap<>();
        this.applicationContext = applicationContext;
    }

    public void doResolver() {
        synchronized (this) {
            for (Map.Entry<Class<?>, BeanResources> entry : this.applicationContext.getBeanResources().entrySet()) {
                for (Map.Entry<String, Object> beanEntry : entry.getValue().getBeans().entrySet()) {
                    this.doResolver(beanEntry.getValue());
                }
            }
            this.applicationContext.getBeanResources().putAll(beanMap);
            this.beanMap.clear();
        }
    }

    private void doResolver(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        if(CommonUtil.empty(methods)) {
            return;
        }
        for (Method method : methods) {
            if(method.isAnnotationPresent(Bean.class)) {
                this.processBeanAnnotation(bean, method, method.getAnnotation(Bean.class));
            }
        }
    }

    @SneakyThrows
    private void processBeanAnnotation(Object o, Method method, Bean bean) {
        if(K.isExclude(method.getReturnType())) {
            log.info("exclude bean class: {}", method.getReturnType());
            return;
        }
        Object obj = method.invoke(o);
        if(CommonUtil.empty(bean.value())) {
            ApplicationContext.registerBean(this.beanMap, method.getReturnType(), obj);
        } else {
            ApplicationContext.registerBean(this.beanMap, bean.value(), method.getReturnType(), obj);
        }
        this.doResolver(obj);
        if(log.isDebugEnabled()) {
            log.debug(": instantiate bean resource: [{}] !", method.getReturnType());
        }
    }
}
