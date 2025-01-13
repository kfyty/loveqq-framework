package com.kfyty.loveqq.framework.codegen.config.annotation;

import com.kfyty.loveqq.framework.codegen.config.AutoGenerateAutoConfig;
import com.kfyty.loveqq.framework.codegen.template.AbstractTemplateEngine;
import com.kfyty.loveqq.framework.codegen.template.enjoy.EnjoyTemplate;
import com.kfyty.loveqq.framework.core.autoconfig.annotation.Import;

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

    Class<? extends AbstractTemplateEngine> templateEngine() default EnjoyTemplate.class;
}
