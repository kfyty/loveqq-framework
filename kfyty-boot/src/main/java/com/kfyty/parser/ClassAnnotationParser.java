package com.kfyty.parser;

import com.kfyty.KfytyApplication;
import com.kfyty.configuration.ApplicationConfigurable;
import com.kfyty.configuration.annotation.Component;
import com.kfyty.configuration.annotation.Configuration;
import com.kfyty.configuration.annotation.EnableAutoGenerateSources;
import com.kfyty.configuration.annotation.KfytyBootApplication;
import com.kfyty.generate.GenerateSources;
import com.kfyty.generate.configuration.GenerateConfiguration;
import com.kfyty.generate.database.AbstractDataBaseMapper;
import com.kfyty.generate.template.AbstractGenerateTemplate;
import com.kfyty.util.CommonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * 功能描述: 类注解解析器
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/23 16:56
 * @since JDK 1.8
 */
@Slf4j
public class ClassAnnotationParser {
    @Getter
    private ApplicationConfigurable applicationConfigurable;

    private MethodAnnotationParser methodAnnotationParser;

    private FieldAnnotationParser fieldAnnotationParser;

    public ApplicationConfigurable initClassAnnotationParser() throws Exception {
        this.applicationConfigurable = ApplicationConfigurable.initApplicationConfigurable();
        this.methodAnnotationParser = new MethodAnnotationParser(applicationConfigurable);
        this.fieldAnnotationParser = new FieldAnnotationParser(applicationConfigurable);
        return this.applicationConfigurable;
    }

    private boolean parseCheck(Class<?> clazz, Set<Class<?>> classSet, boolean ignoredBootAnnotation) {
        if(CommonUtil.empty(classSet)) {
            return false;
        }
        if(clazz == null && !ignoredBootAnnotation) {
            return false;
        }
        if(clazz != null && !clazz.isAnnotationPresent(KfytyBootApplication.class)) {
            return false;
        }
        return true;
    }

    public ApplicationConfigurable parseClassAnnotation(Class<?> clazz, Set<Class<?>> classSet, boolean ignoredBootAnnotation) throws Exception {
        if(!parseCheck(clazz, classSet, ignoredBootAnnotation)) {
            return null;
        }

        this.findAutoConfiguration(classSet);
        this.methodAnnotationParser.parseMethodAnnotation();
        this.fieldAnnotationParser.parseFieldAnnotation();

        this.parseAutoConfiguration();
        this.applicationConfigurable.autoConfigurationAfterCheck();

        this.executeAutoConfiguration(clazz);

        return applicationConfigurable;
    }

    private void findAutoConfiguration(Set<Class<?>> classSet) throws Exception {
        for(Class<?> clazz : classSet) {
           if(clazz.isAnnotationPresent(Configuration.class) || clazz.isAnnotationPresent(Component.class)) {
               this.applicationConfigurable.getBeanResources().put(clazz, CommonUtil.isAbstract(clazz) ? clazz : clazz.newInstance());
               if(log.isDebugEnabled()) {
                   log.debug(": found component: [{}] !", clazz);
               }
           }
        }
    }

    private void parseAutoConfiguration() throws Exception {
        for (Map.Entry<Class<?>, Object> entry : this.applicationConfigurable.getBeanResources().entrySet()) {
            if(entry.getKey().isAnnotationPresent(Configuration.class)) {
                this.parseConfigurationAnnotation(entry.getKey(), entry.getValue());
                continue;
            }
            if(entry.getKey().isAnnotationPresent(Component.class)) {
                this.parseComponentAnnotation(entry.getKey(), entry.getValue());
                continue;
            }
        }
    }

    private void executeAutoConfiguration(Class<?> bootClass) throws Exception {
        if(bootClass == null) {
            return;
        }
        if(bootClass.isAnnotationPresent(EnableAutoGenerateSources.class)) {
            this.applicationConfigurable.executeAutoGenerateSources();
        }
    }

    private void parseConfigurationAnnotation(Class<?> clazz, Object value) throws Exception {
        if(GenerateConfiguration.class.isAssignableFrom(clazz)) {
            this.applicationConfigurable.getGenerateConfigurable().refreshGenerateConfiguration((GenerateConfiguration) value);
            KfytyApplication.getResources(GenerateSources.class).refreshGenerateConfigurable(this.applicationConfigurable.getGenerateConfigurable());
        }
    }

    private void parseComponentAnnotation(Class<?> clazz, Object value) throws Exception {
        if(AbstractDataBaseMapper.class.isAssignableFrom(clazz)) {
            this.applicationConfigurable.getGenerateConfigurable().setDataBaseMapper((Class<? extends AbstractDataBaseMapper>) clazz);
        }
        if(AbstractGenerateTemplate.class.isAssignableFrom(clazz)) {
            this.applicationConfigurable.getGenerateConfigurable().getGenerateTemplateList().add((AbstractGenerateTemplate) value);
        }
    }
}
