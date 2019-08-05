package com.kfyty.annotation;

import com.kfyty.annotation.container.SelectLists;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SelectLists.class)
public @interface SelectList {
    String value() default "";
}
