package com.kfyty.annotation;

import com.kfyty.annotation.container.SelectOnes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SelectOnes.class)
public @interface SelectOne {
    String value() default "";
}
