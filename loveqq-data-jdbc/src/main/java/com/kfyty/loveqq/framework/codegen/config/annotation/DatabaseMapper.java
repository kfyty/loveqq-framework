package com.kfyty.loveqq.framework.codegen.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseMapper {
    Class<? extends com.kfyty.loveqq.framework.codegen.mapper.DatabaseMapper> value();
}
