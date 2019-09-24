package com.kfyty.generate.configuration.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String pattern() default "([\\s\\S]*)";

    String[] value() default "";

    String queryTableSql() default "";
}
