package com.kfyty.annotation;

import com.kfyty.annotation.container.SelectOnes;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SelectOnes.class)
public @interface SelectOne {
    String value() default "";
}
