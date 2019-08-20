package com.kfyty.generate.pojo.annotation;

import com.kfyty.generate.pojo.template.GeneratePojoTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateTemplate {
    Class<? extends GeneratePojoTemplate> value();
}
