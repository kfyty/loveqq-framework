package com.kfyty.support.autoconfig.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 自动配置注解
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ComponentFilter(annotations = Component.class)
public @interface EnableAutoConfiguration {
    /**
     * 排除 k.factories 中的配置类，被排除的类的一切嵌套配置将不会被加载
     * 如果需要排除非 k.factories 中的类，请使用 ComponentScan#excludeFilter
     */
    Class<?>[] exclude() default {};

    /**
     * 作用同 exclude()，需指定配置类的全限定名称
     */
    String[] excludeNames() default {};
}
