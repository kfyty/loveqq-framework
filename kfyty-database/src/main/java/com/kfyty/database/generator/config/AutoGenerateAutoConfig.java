package com.kfyty.database.generator.config;

import com.kfyty.database.generator.GenerateSources;
import com.kfyty.database.generator.config.annotation.EnableAutoGenerate;
import com.kfyty.database.generator.mapper.AbstractDatabaseMapper;
import com.kfyty.database.generator.template.GeneratorTemplate;
import com.kfyty.database.jdbc.intercept.QueryInterceptor;
import com.kfyty.database.jdbc.session.SqlSessionProxyFactory;
import com.kfyty.support.autoconfig.ApplicationContext;
import com.kfyty.support.autoconfig.annotation.Autowired;
import com.kfyty.support.autoconfig.annotation.Bean;
import com.kfyty.support.autoconfig.annotation.Configuration;
import com.kfyty.support.autoconfig.annotation.EventListener;
import com.kfyty.support.autoconfig.annotation.Lazy;
import com.kfyty.support.event.ContextRefreshedEvent;
import com.kfyty.support.utils.AnnotationUtil;
import com.kfyty.support.utils.CommonUtil;
import com.kfyty.support.utils.ReflectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * 描述: 生成资源自动配置
 *
 * @author kfyty725
 * @date 2021/5/21 17:20
 * @email kfyty725@hotmail.com
 */
@Slf4j
@Configuration
public class AutoGenerateAutoConfig {
    @Autowired(required = false)
    private GeneratorConfigurationSupport configurationSupport;

    @Autowired(required = false)
    private List<GeneratorTemplate> templates;

    @Autowired(required = false)
    private GenerateSources generateSources;

    @Lazy
    @Autowired(required = false)
    private SqlSessionProxyFactory sqlSessionProxyFactory;

    @Lazy
    @Autowired(required = false)
    private Class<? extends AbstractDatabaseMapper> databaseMapper;

    @Bean
    public QueryInterceptor fieldStructInfoInterceptor() {
        return new FieldStructInfoInterceptor();
    }

    @SneakyThrows
    @EventListener
    public void onContextRefreshed(ContextRefreshedEvent event) {
        if (this.configurationSupport == null) {
            log.warn("generator configuration does not exist !");
            return;
        }
        GenerateSources generateSources = this.createGenerateSources(event.getSource());
        if (CommonUtil.notEmpty(generateSources.getConfiguration().getTemplateList())) {
            generateSources.doGenerate();
        }
    }

    protected GenerateSources createGenerateSources(ApplicationContext applicationContext) {
        GenerateSources generateSources = ofNullable(this.generateSources).orElse(new GenerateSources()).refreshConfiguration(this.configurationSupport);
        EnableAutoGenerate annotation = AnnotationUtil.findAnnotation(applicationContext.getPrimarySource(), EnableAutoGenerate.class);
        if (annotation.loadTemplate()) {
            List<? extends GeneratorTemplate> templates = ReflectUtil.newInstance(annotation.templateEngine()).loadTemplates(annotation.templatePrefix());
            generateSources.refreshTemplate(templates);
            if (CommonUtil.empty(templates)) {
                log.warn("no template found for prefix: '" + annotation.templatePrefix() + "' !");
            }
        }
        if (this.sqlSessionProxyFactory != null) {
            generateSources.setSqlSessionProxyFactory(this.sqlSessionProxyFactory);
        }
        if (this.templates != null) {
            generateSources.refreshTemplate(this.templates);
        }
        if (databaseMapper != null) {
            generateSources.getConfiguration().setDatabaseMapper(databaseMapper);
        }
        return generateSources;
    }
}
