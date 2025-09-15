package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.beans.BeanDefinition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记优先级
 * 可以用于 bean 实例排序，也可以用于 {@link BeanDefinition} 排序
 *
 * @author kfyty725
 * @date 2021/6/13 11:28
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.core.utils.BeanUtil#getBeanOrder(BeanDefinition)
 * @see com.kfyty.loveqq.framework.core.utils.BeanUtil#getBeanOrder(Object)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Order {
    /**
     * 最高优先级的值
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE + 1;

    /**
     * 最低优先级的值
     */
    int LOWEST_PRECEDENCE = Integer.MAX_VALUE - 1;

    /**
     * 默认优先级的值
     */
    int DEFAULT_PRECEDENCE = LOWEST_PRECEDENCE >> 1;

    /**
     * 值越小优先级越高
     * 默认值是 {@link #DEFAULT_PRECEDENCE} - 999_999_999，表示比默认优先级更高，方便第三方在其中控制顺序
     *
     * @return {@link #DEFAULT_PRECEDENCE}
     */
    int value() default DEFAULT_PRECEDENCE - 999_999_999;
}
