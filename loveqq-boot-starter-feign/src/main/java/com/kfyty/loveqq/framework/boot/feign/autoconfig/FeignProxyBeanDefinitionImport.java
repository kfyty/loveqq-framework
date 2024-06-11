package com.kfyty.loveqq.framework.boot.feign.autoconfig;

import com.kfyty.loveqq.framework.boot.feign.autoconfig.annotation.FeignClient;
import com.kfyty.loveqq.framework.boot.feign.autoconfig.factory.RibbonFeignFactoryBean;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ImportBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import com.kfyty.loveqq.framework.core.utils.ScopeUtil;

import java.util.function.Predicate;

/**
 * 描述: 导入 feign 代理
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@Component
public class FeignProxyBeanDefinitionImport implements ImportBeanDefinition {

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return e -> AnnotationUtil.hasAnnotation(e, FeignClient.class);
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        Scope scope = ScopeUtil.resolveScope(clazz);
        return BeanDefinitionBuilder.genericBeanDefinition(RibbonFeignFactoryBean.class)
                .setScope(scope.value())
                .setScopeProxy(scope.scopeProxy())
                .addConstructorArgs(Class.class, clazz)
                .getBeanDefinition();
    }
}
