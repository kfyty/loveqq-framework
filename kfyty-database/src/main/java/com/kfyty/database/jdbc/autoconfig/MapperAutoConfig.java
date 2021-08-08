package com.kfyty.database.jdbc.autoconfig;

import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder;
import com.kfyty.support.utils.AnnotationUtil;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述: 自动配置 Mapper 注解
 *
 * @author kfyty725
 * @date 2021/5/22 13:13
 * @email kfyty725@hotmail.com
 */
@Configuration
public class MapperAutoConfig implements ImportBeanDefine {

    @Bean
    public SqlSessionProxyFactoryBean sqlSessionProxyFactoryBean() {
        return new SqlSessionProxyFactoryBean();
    }

    @Override
    public Set<BeanDefinition> doImport(Set<Class<?>> scanClasses) {
        return scanClasses
                .stream()
                .filter(e -> AnnotationUtil.hasAnnotation(e, Mapper.class))
                .map(e -> BeanDefinitionBuilder.genericBeanDefinition(MapperInterfaceFactoryBean.class).addConstructorArgs(Class.class, e).getBeanDefinition())
                .collect(Collectors.toSet());
    }
}
