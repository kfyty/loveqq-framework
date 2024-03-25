package com.kfyty.database.generator.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseMapper {
    Class<? extends com.kfyty.database.generator.mapper.DatabaseMapper> value();
}
