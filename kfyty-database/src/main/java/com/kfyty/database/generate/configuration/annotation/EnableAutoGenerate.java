package com.kfyty.database.generate.configuration.annotation;

import com.kfyty.database.generate.configuration.GenerateAutoConfig;
import com.kfyty.database.generate.template.AbstractTemplateEngine;
import com.kfyty.database.generate.template.freemarker.FreemarkerTemplate;
import com.kfyty.support.autoconfig.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(config = GenerateAutoConfig.class)
public @interface EnableAutoGenerate {

    boolean loadTemplate() default true;

    String templatePrefix() default "";

    Class<? extends AbstractTemplateEngine> templateEngine() default FreemarkerTemplate.class;
}
