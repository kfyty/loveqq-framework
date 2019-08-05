package com.kfyty.annotation;

import com.kfyty.annotation.container.Executes;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Executes.class)
public @interface Execute {
    String value() default "";
}
