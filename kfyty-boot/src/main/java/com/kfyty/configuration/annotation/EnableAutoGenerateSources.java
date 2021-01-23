package com.kfyty.configuration.annotation;

import com.kfyty.generate.template.AbstractTemplateEngine;
import com.kfyty.generate.template.freemarker.FreemarkerTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableAutoGenerateSources {
    boolean loadTemplate() default true;

    String templatePrefix() default "";

    Class<? extends AbstractTemplateEngine> templateEngine() default FreemarkerTemplate.class;
}
