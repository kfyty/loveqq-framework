package com.kfyty.boot.autoconfig.support;

import com.kfyty.boot.autoconfig.factory.LookupBeanFactoryBean;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.ImportBeanDefinition;
import com.kfyty.support.autoconfig.annotation.Lookup;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

import static com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.support.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.support.utils.ReflectUtil.getMethods;
import static com.kfyty.support.utils.ReflectUtil.isAbstract;

/**
 * 描述: 导入存在 Lookup 注解的抽象方法的 bean 定义
 *
 * @author kfyty725
 * @date 2021/7/11 12:40
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class LookupBeanDefinitionImport implements ApplicationContextAware, ImportBeanDefinition {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return e -> isAbstract(e) && this.applicationContext.doFilterComponent(e) && getMethods(e).stream().anyMatch(m -> hasAnnotation(m, Lookup.class));
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(LookupBeanFactoryBean.class).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}
