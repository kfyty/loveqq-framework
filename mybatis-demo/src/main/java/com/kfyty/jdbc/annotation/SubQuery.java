package com.kfyty.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubQuery {
    String query();

    String returnField();

    boolean returnSingle() default false;

    String[] paramField();

    String[] mapperField();
}
