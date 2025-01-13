package com.kfyty.loveqq.framework.codegen.config;

import com.kfyty.loveqq.framework.codegen.mapper.DatabaseMapper;
import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ImportBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Component;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;

import java.util.function.Predicate;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2022/10/25 18:11
 * @email kfyty725@hotmail.com
 */
@Component
public class DatabaseMapperImporter implements ImportBeanDefinition {

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return DatabaseMapper.class::isAssignableFrom;
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(DatabaseMapperFactory.class).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}
