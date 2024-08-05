package com.kfyty.loveqq.framework.core.lang.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 为注解指定别名
 * <p>
 * 禁止使用 {@link AliasFor} 注解 {@link AliasFor} 本身
 *
 * @author kfyty725
 * @date 2024/8/5 11:16
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AliasFor {
    /**
     * 元数据的别名，默认和被注解的方法同名
     *
     * @return alias
     */
    String value() default "";

    /**
     * 注解别名所在的 class，默认是当前注解类型
     *
     * @return 注解类型
     */
    Class<? extends Annotation> annotation() default Annotation.class;
}
