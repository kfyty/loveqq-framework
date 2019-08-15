package com.kfyty.jdbc.annotation;

import com.kfyty.jdbc.annotation.container.SelectOnes;

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

    SubQuery[] subQuery() default {};
}
