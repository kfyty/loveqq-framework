package com.kfyty.database.generate.configuration.annotation;

import com.kfyty.database.generate.database.AbstractDataBaseMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBaseMapper {
    Class<? extends AbstractDataBaseMapper> value();
}
