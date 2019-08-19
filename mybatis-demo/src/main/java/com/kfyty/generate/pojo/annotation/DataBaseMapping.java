package com.kfyty.generate.pojo.annotation;

import com.kfyty.generate.pojo.database.DataBaseMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBaseMapping {
    Class<? extends DataBaseMapper> value();
}
