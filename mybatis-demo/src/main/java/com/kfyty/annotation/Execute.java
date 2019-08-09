package com.kfyty.annotation;

import com.kfyty.annotation.container.Executes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Executes.class)
public @interface Execute {
    String value() default "";
}
