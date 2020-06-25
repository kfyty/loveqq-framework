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
import com.kfyty.generate.template.freemarker.FreemarkerTemplate;
import com.kfyty.jdbc.SqlSession;
import com.kfyty.mvc.annotation.Controller;
import com.kfyty.mvc.annotation.Mapper;
import com.kfyty.mvc.annotation.Repository;
import com.kfyty.mvc.annotation.RestController;
import com.kfyty.mvc.annotation.Service;
import com.kfyty.mvc.handler.MVCAnnotationHandler;
import com.kfyty.util.CommonUtil;
import com.kfyty.util.LoadFreemarkerTemplateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
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
        this.parseBootConfiguration(clazz);
        this.applicationConfigurable.autoConfigurationAfterCheck();

        this.executeAutoConfiguration(clazz);

        return applicationConfigurable;
    }

    private Object newInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        if(!CommonUtil.isAbstract(clazz)) {
            return clazz.newInstance();
        }
        if(clazz.isInterface() && clazz.isAnnotationPresent(Mapper.class)) {
            return KfytyApplication.getResources(SqlSession.class).getProxyObject(clazz);
        }
        if(AbstractDataBaseMapper.class.isAssignableFrom(clazz)) {
            return null;
        }
        throw new InstantiationException(CommonUtil.fillString("cannot instance for abstract class: [{}]", clazz));
    }

    private void findAutoConfiguration(Set<Class<?>> classSet) {
        classSet.stream()
                .filter(e ->
                        e.isAnnotationPresent(Configuration.class)      ||
                        e.isAnnotationPresent(Component.class)          ||
                        e.isAnnotationPresent(Controller.class)         ||
                        e.isAnnotationPresent(RestController.class)     ||
                        e.isAnnotationPresent(Service.class)            ||
                        e.isAnnotationPresent(Repository.class)         ||
                        e.isAnnotationPresent(Mapper.class))
                .forEach(e -> {
                    try {
                        this.applicationConfigurable.getBeanResources().put(e, this.newInstance(e));
                        if(log.isDebugEnabled()) {
                            log.debug(": found component: [{}] !", e);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
    }

    private void parseAutoConfiguration() throws Exception {
        Map<Class<?>, Object> beanMap = new HashMap<>(this.applicationConfigurable.getBeanResources());
        for (Map.Entry<Class<?>, Object> entry : beanMap.entrySet()) {
            if(entry.getKey().isAnnotationPresent(Configuration.class)) {
                this.parseConfigurationAnnotation(entry.getKey(), entry.getValue());
                continue;
            }
            if(entry.getKey().isAnnotationPresent(Component.class)) {
                this.parseComponentAnnotation(entry.getKey(), entry.getValue());
                continue;
            }
            if(entry.getKey().isAnnotationPresent(Controller.class) || entry.getKey().isAnnotationPresent(RestController.class)) {
                this.parseControllerAnnotation(entry.getKey(), entry.getValue());
                continue;
            }
        }
    }

    private void parseBootConfiguration(Class<?> bootClass) throws Exception {
        if(bootClass.isAnnotationPresent(EnableAutoGenerateSources.class)) {
            this.parseEnableAutoGenerateSources(bootClass.getAnnotation(EnableAutoGenerateSources.class));
        }
    }

    private void executeAutoConfiguration(Class<?> bootClass) throws Exception {
        if(bootClass == null || !bootClass.isAnnotationPresent(KfytyBootApplication.class)) {
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

    private void parseControllerAnnotation(Class<?> clazz, Object value) throws Exception {
        MVCAnnotationHandler mvcAnnotationHandler = KfytyApplication.getResources(MVCAnnotationHandler.class);
        mvcAnnotationHandler.setMappingController(value);
        mvcAnnotationHandler.buildURLMappingMap();
    }

    private void parseEnableAutoGenerateSources(EnableAutoGenerateSources annotation) throws Exception {
        if(!annotation.loadTemplate()) {
            return;
        }
        List<FreemarkerTemplate> templates = LoadFreemarkerTemplateUtil.loadTemplates(annotation.templatePrefix());
        if(CommonUtil.empty(templates)) {
            log.warn(": No freemarker template found for prefix: '" + annotation.templatePrefix() + "' !");
            return;
        }
        GenerateSources generateSources = ((GenerateSources) this.applicationConfigurable.getBeanResources().get(GenerateSources.class));
        generateSources.refreshGenerateTemplate(templates);
    }
}
