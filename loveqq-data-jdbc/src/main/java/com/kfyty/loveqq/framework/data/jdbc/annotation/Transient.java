package com.kfyty.loveqq.framework.data.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表示忽略的表字段
 *
 * @author kfyty725
 * @date 2021/7/22 18:35
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient {
}
