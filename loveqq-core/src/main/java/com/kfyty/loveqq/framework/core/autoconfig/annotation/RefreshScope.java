package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Documented;
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
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Scope(BeanDefinition.SCOPE_REFRESH)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RefreshScope {
    /**
     * 是否使用作用域代理
     *
     * @return 默认 true
     */
    @AliasFor(annotation = Scope.class)
    boolean scopeProxy() default true;
}
