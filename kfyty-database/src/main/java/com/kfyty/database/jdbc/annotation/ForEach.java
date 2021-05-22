package com.kfyty.database.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ForEach {

    String collection();

    String item();

    String open() default "";

    String sqlPart();

    String separator();

    String close() default "";
}
