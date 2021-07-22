package com.kfyty.database.jdbc.annotation;

import com.kfyty.database.jdbc.sql.Provider;

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

    Class<? extends Provider> provider() default Provider.class;

    String method() default "";
}
