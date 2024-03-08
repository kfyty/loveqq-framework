package com.kfyty.boot.autoconfig.support;

import com.kfyty.boot.autoconfig.factory.LookupBeanFactoryBean;
import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.ConfigurableApplicationContext;
import com.kfyty.core.autoconfig.annotation.Scope;
import com.kfyty.core.autoconfig.aware.ConfigurableApplicationContextAware;
import com.kfyty.core.autoconfig.ImportBeanDefinition;
import com.kfyty.core.autoconfig.annotation.Lookup;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.utils.ScopeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Predicate;

import static com.kfyty.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.core.utils.ReflectUtil.getMethods;
import static com.kfyty.core.utils.ReflectUtil.isAbstract;

/**
 * 描述: 导入存在 Lookup 注解的抽象方法的 bean 定义
 *
 * @author kfyty725
 * @date 2021/7/11 12:40
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class LookupBeanDefinitionImport implements ConfigurableApplicationContextAware, ImportBeanDefinition {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return e -> isAbstract(e) && this.applicationContext.doFilterComponent(e) && getMethods(e).stream().anyMatch(m -> hasAnnotation(m, Lookup.class));
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        Scope scope = ScopeUtil.resolveScope(clazz);
        return genericBeanDefinition(LookupBeanFactoryBean.class).setScope(scope.value()).setScopeProxy(scope.scopeProxy()).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}
