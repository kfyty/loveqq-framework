package com.kfyty.loveqq.framework.core.autoconfig.condition.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.condition.OnClassCondition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 类路径 class 存在条件
 *
 * @author kfyty725
 * @date 2022/4/17 11:41
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Conditional(OnClassCondition.class)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ConditionalOnClass {
    /**
     * 类路径上应该存在的 class
     *
     * @return class 的全限定名
     */
    String[] value() default {};

    /**
     * 类路径上应该存在的 class
     * <p>
     * jdk 高版本可用，因为高版本当 class 不存在时，获取注解元数据不在抛出异常
     * jdk 低版本建议使用 {@link this#value()}
     *
     * @return class
     */
    Class<?>[] classes() default {};
}
