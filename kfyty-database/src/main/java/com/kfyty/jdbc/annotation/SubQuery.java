package com.kfyty.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubQuery {
    String value();

    String key() default "";

    String returnField();

    String[] paramField();

    String[] mapperField();

    ForEach[] forEach() default {};
}
