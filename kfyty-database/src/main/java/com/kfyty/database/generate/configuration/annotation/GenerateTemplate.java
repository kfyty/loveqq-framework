package com.kfyty.database.generate.configuration.annotation;

import com.kfyty.database.generate.template.AbstractGenerateTemplate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GenerateTemplate {
    Class<? extends AbstractGenerateTemplate>[] value();
}
