package com.kfyty.support.autoconfig.annotation;

import com.kfyty.support.autoconfig.beans.BeanDefinition;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: bean 作用域
 *
 * @author kfyty725
 * @date 2021/7/11 10:40
 * @email kfyty725@hotmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Scope {
    /**
     * 作用域，默认为单例
     * @return SCOPE
     */
    String value() default BeanDefinition.SCOPE_SINGLETON;

    /**
     * 是否对非单例作用域使用代理。
     * 若为 true，则会自动为该 bean 创建一个代理，每次调用其方法时，都会实例化一个新的 bean，并以该新的实例调用该方法
     * 若为 false，则该 bean 只有在使用的时候才会进行实例化，若在单例 bean 中使用时，可使用 Lookup 注解的抽象方法获取新的实例
     * @return true if use scope proxy
     */
    boolean scopeProxy() default false;
}
