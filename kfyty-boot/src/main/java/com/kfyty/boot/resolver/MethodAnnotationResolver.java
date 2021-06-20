package com.kfyty.boot.resolver;

import com.kfyty.boot.configuration.DefaultApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.beans.AutowiredProcessor;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.AopUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

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
    private final DefaultApplicationContext applicationContext;
    private final AutowiredProcessor autowiredProcessor;

    public MethodAnnotationResolver(AnnotationConfigResolver configResolver) {
        this.configResolver = configResolver;
        this.applicationContext = configResolver.getApplicationContext();
        this.autowiredProcessor =  configResolver.getAutowiredProcessor();
    }

    /**
     * 解析该 BeanDefinition 中可能存在的其他 BeanDefinition
     * @param beanDefinition BeanDefinition
     */
    public void prepareBeanDefines(BeanDefinition beanDefinition) {
        Method[] methods = beanDefinition.getBeanType().getMethods();
        for (Method method : methods) {
            if(AnnotationUtil.hasAnnotation(method, Bean.class)) {
                BeanDefinition methodBeanDefinition = GenericBeanDefinition.from(beanDefinition, method, AnnotationUtil.findAnnotation(method, Bean.class));
                this.configResolver.registerBeanDefinition(methodBeanDefinition);
            }
        }
    }

    /**
     * 对容器内的所有 bean 执行方法注入
     */
    public void doResolver() {
        this.applicationContext.doInBeans((beanName, bean) -> this.doResolver(bean));
    }

    /**
     * 对特定的 bean 执行方法注入
     * 如果该 bean 是 jdk 代理，则对原对象执行注入
     * @param bean bean 实例
     */
    public void doResolver(Object bean) {
        bean = AopUtil.getSourceIfNecessary(bean);
        Method[] methods = bean.getClass().getMethods();
        for (Method method : methods) {
            if(AnnotationUtil.hasAnnotation(method, Autowired.class)) {
                this.autowiredProcessor.doAutowired(bean, method);
            }
        }
    }
}
