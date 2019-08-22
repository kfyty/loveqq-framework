package com.kfyty.generate.annotation;

import com.kfyty.generate.template.GenerateTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratingTemplate {
    Class<? extends GenerateTemplate> value();
}
