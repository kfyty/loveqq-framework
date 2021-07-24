package com.kfyty.database.jdbc.annotation;

import com.kfyty.database.jdbc.annotation.container.Queries;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Queries.class)
public @interface Query {

    String value();

    String key() default "";

    ForEach[] forEach() default {};

    SubQuery[] subQuery() default {};

    Class<?> provider() default void.class;

    String method() default "";
}
