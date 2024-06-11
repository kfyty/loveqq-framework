package com.kfyty.loveqq.framework.codegen.config.annotation;

import com.kfyty.loveqq.framework.codegen.template.GeneratorTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Template {
    Class<? extends GeneratorTemplate>[] value();
}
