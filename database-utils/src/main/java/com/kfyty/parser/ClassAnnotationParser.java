package com.kfyty.parser;

import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.ApplyGenerateTemplate;
import com.kfyty.configuration.annotation.AutoGenerateSources;
import com.kfyty.configuration.annotation.DataBaseMapping;
import com.kfyty.configuration.annotation.GenerateSourcesConfiguration;
import com.kfyty.generate.configuration.GenerateConfiguration;
import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 功能描述: 类注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:56
 * @since JDK 1.8
 */
@NoArgsConstructor
public class ClassAnnotationParser {
    @Getter
    private ApplicationConfigurable configuration;

    public ApplicationConfigurable parseClassAnnotation(Class<?> clazz, Set<Class<?>> classSet) throws Exception {
        if(CommonUtil.empty(classSet)) {
            return null;
        }
        this.configuration = new ApplicationConfigurable();
        this.configuration.initApplicationConfigurable();
        if(clazz.isAnnotationPresent(AutoGenerateSources.class)) {
            this.parseAutoGenerateSourcesConfiguration(classSet);
        }
        return configuration;
    }

    private void parseAutoGenerateSourcesConfiguration(Set<Class<?>> classSet) throws Exception {
        this.configuration.getGenerateConfigurable().setAutoGenerate(true);
        for(Class<?> clazz : classSet) {
           if(clazz.isAnnotationPresent(GenerateSourcesConfiguration.class)) {
               this.configuration.getGenerateConfigurable().refreshGenerateConfiguration((GenerateConfiguration) clazz.newInstance());
           }
           if(clazz.isAnnotationPresent(DataBaseMapping.class)) {
               this.configuration.getGenerateConfigurable().setDataBaseMapper((Class<? extends AbstractDataBaseMapper>) clazz);
           }
           if(clazz.isAnnotationPresent(ApplyGenerateTemplate.class)) {
               this.configuration.getGenerateConfigurable().getGenerateTemplateSet().add((AbstractGenerateTemplate) clazz.newInstance());
           }
        }
        this.configuration.getGenerateConfigurable().autoConfigurationAfterCheck();
        this.configuration.executeAutoGenerateSources();
    }
}
