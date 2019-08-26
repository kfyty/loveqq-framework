package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.configuration.annotation.EnableAutoGenerateSources;
import com.kfyty.configuration.annotation.KfytyBootApplication;
import com.kfyty.generate.configuration.GenerateConfiguration;
import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * 功能描述: 类注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:56
 * @since JDK 1.8
 */
@Slf4j
@NoArgsConstructor
public class ClassAnnotationParser {
    @Getter
    private ApplicationConfigurable configuration;

    public ApplicationConfigurable parseClassAnnotation(Class<?> clazz, Set<Class<?>> classSet) throws Exception {
        if(CommonUtil.empty(classSet) || !clazz.isAnnotationPresent(KfytyBootApplication.class)) {
            return null;
        }
        this.configuration = ApplicationConfigurable.initApplicationConfigurable();
        this.configuration.initAutoConfiguration();
        this.parseAutoConfiguration(classSet);
        this.configuration.autoConfigurationAfterCheck();
        if(clazz.isAnnotationPresent(EnableAutoGenerateSources.class)) {
            this.configuration.executeAutoGenerateSources();
        }
        return configuration;
    }

    private void parseAutoConfiguration(Set<Class<?>> classSet) throws Exception {
        for(Class<?> clazz : classSet) {
           if(clazz.isAnnotationPresent(Configuration.class)) {
               this.parseConfigurationAnnotation(clazz);
               if(log.isDebugEnabled()) {
                   log.debug(": found auto configuration: [{}] !", clazz);
               }
           }
           if(clazz.isAnnotationPresent(Component.class)) {
               this.parseComponentAnnotation(clazz);
               if(log.isDebugEnabled()) {
                   log.debug(": found component: [{}] !", clazz);
               }
           }
        }
    }

    private void parseConfigurationAnnotation(Class<?> clazz) throws Exception {
        if(GenerateConfiguration.class.isAssignableFrom(clazz)) {
            this.configuration.getGenerateConfigurable().refreshGenerateConfiguration((GenerateConfiguration) clazz.newInstance());
        }
    }

    private void parseComponentAnnotation(Class<?> clazz) throws Exception {
        if(AbstractDataBaseMapper.class.isAssignableFrom(clazz)) {
            this.configuration.getGenerateConfigurable().setDataBaseMapper((Class<? extends AbstractDataBaseMapper>) clazz);
        }
        if(AbstractGenerateTemplate.class.isAssignableFrom(clazz)) {
            this.configuration.getGenerateConfigurable().getGenerateTemplateList().add((AbstractGenerateTemplate) clazz.newInstance());
        }
    }
}
