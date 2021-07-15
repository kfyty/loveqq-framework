package com.kfyty.database.generator.config.annotation;

import com.kfyty.database.generator.template.GeneratorTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Template {
    Class<? extends GeneratorTemplate>[] value();
}
