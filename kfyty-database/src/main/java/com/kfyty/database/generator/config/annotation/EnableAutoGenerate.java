package com.kfyty.database.generator.config.annotation;

import com.kfyty.database.generator.config.AutoGenerateAutoConfig;
import com.kfyty.database.generator.template.AbstractTemplateEngine;
import com.kfyty.database.generator.template.freemarker.FreemarkerTemplate;
import com.kfyty.core.autoconfig.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = AutoGenerateAutoConfig.class)
public @interface EnableAutoGenerate {

    boolean loadTemplate() default true;

    String templatePrefix() default "";

    Class<? extends AbstractTemplateEngine> templateEngine() default FreemarkerTemplate.class;
}
