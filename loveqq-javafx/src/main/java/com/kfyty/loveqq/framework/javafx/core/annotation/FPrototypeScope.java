package com.kfyty.loveqq.framework.javafx.core.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Scope;
import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: javafx 作用域，用于 {@link FController}
 * 表示原型作用域，且不使用作用域代理
 *
 * @author kfyty725
 * @date 2021/7/11 10:40
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Scope(value = BeanDefinition.SCOPE_PROTOTYPE, scopeProxy = false)
public @interface FPrototypeScope {
}
