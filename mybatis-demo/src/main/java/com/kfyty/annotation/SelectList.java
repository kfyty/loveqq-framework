package com.kfyty.annotation;

import com.kfyty.annotation.container.SelectLists;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SelectLists.class)
public @interface SelectList {
    String value() default "";
}
