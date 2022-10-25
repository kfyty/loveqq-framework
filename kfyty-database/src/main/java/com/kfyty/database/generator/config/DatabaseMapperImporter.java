package com.kfyty.database.generator.config;

import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ImportBeanDefinition;
import com.kfyty.support.autoconfig.annotation.Component;
import com.kfyty.support.autoconfig.beans.BeanDefinition;

import java.util.function.Predicate;

import static com.kfyty.support.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

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
        return AbstractDatabaseMapper.class::isAssignableFrom;
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(DatabaseMapperFactory.class).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}
