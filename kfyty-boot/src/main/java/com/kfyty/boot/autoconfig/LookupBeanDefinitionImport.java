package com.kfyty.boot.autoconfig;

import com.kfyty.boot.autoconfig.factory.LookupBeanFactoryBean;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ApplicationContextAware;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.annotation.Lookup;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.ReflectUtil;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述: 导入存在 Lookup 注解的抽象方法的 bean 定义
 *
 * @author kfyty725
 * @date 2021/7/11 12:40
 * @email kfyty725@hotmail.com
 */
@Component
public class LookupBeanDefinitionImport implements ApplicationContextAware, ImportBeanDefine {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Set<BeanDefinition> doImport(Set<Class<?>> scanClasses) {
        return scanClasses.parallelStream()
                .filter(e -> ReflectUtil.isAbstract(e) && this.applicationContext.doFilterComponent(e))
                .filter(e -> ReflectUtil.getMethods(e).stream().anyMatch(m -> AnnotationUtil.hasAnnotation(m, Lookup.class)))
                .map(e -> BeanDefinitionBuilder.genericBeanDefinition(LookupBeanFactoryBean.class).addConstructorArgs(Class.class, e).getBeanDefinition())
                .collect(Collectors.toSet());
    }
}
