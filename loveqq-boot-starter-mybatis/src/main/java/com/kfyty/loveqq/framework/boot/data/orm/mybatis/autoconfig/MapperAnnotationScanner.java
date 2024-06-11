package com.kfyty.loveqq.framework.boot.data.orm.mybatis.autoconfig;

import com.kfyty.loveqq.framework.core.autoconfig.ApplicationContext;
import com.kfyty.loveqq.framework.core.autoconfig.ImportBeanDefinition;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.utils.AnnotationUtil;
import org.apache.ibatis.annotations.Mapper;

import java.util.function.Predicate;

import static com.kfyty.loveqq.framework.core.autoconfig.beans.builder.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * 描述: mapper 注解接口扫描器
 *
 * @author kfyty725
 * @date 2024/6/03 18:55
 * @email kfyty725@hotmail.com
 */
public class MapperAnnotationScanner implements ImportBeanDefinition {

    @Override
    public Predicate<Class<?>> classesFilter(ApplicationContext applicationContext) {
        return e -> AnnotationUtil.hasAnnotation(e, Mapper.class);
    }

    @Override
    public BeanDefinition buildBeanDefinition(ApplicationContext applicationContext, Class<?> clazz) {
        return genericBeanDefinition(MapperInterfaceFactoryBean.class).addConstructorArgs(Class.class, clazz).getBeanDefinition();
    }
}
