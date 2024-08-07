package com.kfyty.loveqq.framework.core.lang.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 表示查询该类的注解时，应该递归查询父类注解，包括构造器、方法、方法参数
 *
 * @author kfyty725
 * @date 2024/8/8 10:42
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.TYPE)
@java.lang.annotation.Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Inherited {
}
