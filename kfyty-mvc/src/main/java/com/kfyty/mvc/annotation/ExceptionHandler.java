package com.kfyty.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 异常处理
 *
 * @author kfyty725
 * @date 2021/6/18 11:02
 * @email kfyty725@hotmail.com
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    Class<? extends Throwable>[] value();
}
