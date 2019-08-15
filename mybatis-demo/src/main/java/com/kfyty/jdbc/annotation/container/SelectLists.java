package com.kfyty.jdbc.annotation.container;

import com.kfyty.jdbc.annotation.SelectList;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelectLists {
    SelectList[] value();
}
