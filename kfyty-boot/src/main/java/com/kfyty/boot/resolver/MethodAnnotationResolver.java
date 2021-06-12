package com.kfyty.boot.resolver;

import com.kfyty.boot.beans.BeanResources;
import com.kfyty.boot.configuration.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.beans.AutowiredProcessor;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
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
    private final AnnotationConfigResolver configResolver;
    private final ApplicationContext applicationContext;
    private final AutowiredProcessor autowiredProcessor;

    public MethodAnnotationResolver(AnnotationConfigResolver configResolver) {
        this.configResolver = configResolver;
        this.applicationContext = configResolver.getApplicationContext();
        this.autowiredProcessor =  configResolver.getAutowiredProcessor();
    }

    public void prepareBeanDefines(BeanDefinition beanDefinition) {
        Method[] methods = beanDefinition.getBeanType().getMethods();
        for (Method method : methods) {
            if(method.isAnnotationPresent(Bean.class)) {
                BeanDefinition methodBeanDefinition = GenericBeanDefinition.from(beanDefinition, method, method.getAnnotation(Bean.class));
                this.configResolver.addBeanDefinition(methodBeanDefinition);
            }
        }
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
            if(method.isAnnotationPresent(Autowired.class)) {
                this.autowiredProcessor.doAutowired(bean, method);
            }
        }
    }
}
