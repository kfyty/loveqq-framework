package com.kfyty.database.generator.config;

import com.kfyty.database.generator.GenerateSources;
import com.kfyty.database.generator.config.annotation.EnableAutoGenerate;
import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.ContextRefreshCompleted;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.Lazy;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 描述: 生成资源自动配置
 *
 * @author kfyty725
 * @date 2021/5/21 17:20
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Configuration
public class AutoGenerateAutoConfig implements ImportBeanDefine, ContextRefreshCompleted {
    @Autowired(required = false)
    private GeneratorConfigurationSupport configurationSupport;

    @Autowired(required = false)
    private List<GeneratorTemplate> templates;

    @Lazy @Autowired(required = false)
    private Class<? extends AbstractDatabaseMapper> databaseMapper;

    @Override
    public Set<BeanDefinition> doImport(Set<Class<?>> scanClasses) {
        return scanClasses
                .stream()
                .filter(AbstractDatabaseMapper.class::isAssignableFrom)
                .map(e -> GenericBeanDefinition.from(DatabaseMapperFactory.class).addConstructorArgs(Class.class, e))
                .collect(Collectors.toSet());
    }

    @Override
    @SneakyThrows
    public void onCompleted(ApplicationContext applicationContext) {
        if(this.configurationSupport == null) {
            log.warn("generator configuration does not exist !");
            return;
        }
        GenerateSources generateSources = new GenerateSources(this.configurationSupport);
        EnableAutoGenerate annotation = AnnotationUtil.findAnnotation(applicationContext.getPrimarySource(), EnableAutoGenerate.class);
        if(annotation.loadTemplate()) {
            List<? extends GeneratorTemplate> templates = ReflectUtil.newInstance(annotation.templateEngine()).loadTemplates(annotation.templatePrefix());
            generateSources.refreshTemplate(templates);
            if(CommonUtil.empty(templates)) {
                log.warn("no template found for prefix: '" + annotation.templatePrefix() + "' !");
            }
        }
        if(this.templates != null) {
            generateSources.refreshTemplate(this.templates);
        }
        if(databaseMapper != null) {
            generateSources.getConfiguration().setDatabaseMapper(databaseMapper);
        }
        if(!CommonUtil.empty(generateSources.getConfiguration().getTemplateList())) {
            generateSources.doGenerate();
        }
    }
}
