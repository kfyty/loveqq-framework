package com.kfyty.database.generate.configuration;

import com.kfyty.database.generate.GenerateSources;
import com.kfyty.database.generate.configuration.annotation.EnableAutoGenerate;
import com.kfyty.database.generate.database.AbstractDataBaseMapper;
import com.kfyty.database.generate.template.AbstractGenerateTemplate;
import com.kfyty.support.autoconfig.BeanRefreshComplete;
import com.kfyty.support.autoconfig.ImportBeanDefine;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.beans.BeanDefinition;
import com.kfyty.support.autoconfig.beans.GenericBeanDefinition;
import com.kfyty.support.utils.CommonUtil;
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
public class GenerateAutoConfig implements ImportBeanDefine, BeanRefreshComplete {
    @Autowired(required = false)
    private GenerateConfiguration generateConfiguration;

    @Autowired(required = false)
    private Class<? extends AbstractDataBaseMapper> dataBaseMapper;

    @Autowired(required = false)
    private List<AbstractGenerateTemplate> templates;

    @Override
    public Set<BeanDefinition> doImport(Set<Class<?>> scanClasses) {
        return scanClasses
                .stream()
                .filter(AbstractDataBaseMapper.class::isAssignableFrom)
                .map(e -> GenericBeanDefinition.from(DataBaseMapperFactory.class).addConstructorArgs(Class.class, e))
                .collect(Collectors.toSet());
    }

    @Override
    @SneakyThrows
    public void onComplete(Class<?> primarySource, String ... args) {
        if(!primarySource.isAnnotationPresent(EnableAutoGenerate.class)) {
            return;
        }
        if(this.generateConfiguration == null) {
            log.warn("generate config does not exist !");
            return;
        }
        EnableAutoGenerate annotation = primarySource.getAnnotation(EnableAutoGenerate.class);
        List<? extends AbstractGenerateTemplate> templates = annotation.templateEngine().newInstance().loadTemplates(annotation.templatePrefix());
        if(CommonUtil.empty(templates)) {
            log.warn(": No freemarker template found for prefix: '" + annotation.templatePrefix() + "' !");
        }
        GenerateSources generateSources = new GenerateSources(this.generateConfiguration);
        generateSources.refreshGenerateTemplate(templates);
        if(this.templates != null) {
            generateSources.refreshGenerateTemplate(this.templates);
        }
        if(dataBaseMapper != null) {
            generateSources.getConfigurable().setDataBaseMapper(dataBaseMapper);
        }
        if(!CommonUtil.empty(generateSources.getConfigurable().getGenerateTemplateList())) {
            generateSources.generate();
        }
    }
}
