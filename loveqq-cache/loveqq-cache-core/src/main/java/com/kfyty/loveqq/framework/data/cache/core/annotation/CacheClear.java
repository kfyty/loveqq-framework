package com.kfyty.loveqq.framework.data.cache.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 移除缓存注解
 *
 * @author kfyty725
 * @date 2024/7/4 9:52
 * @email kfyty725@hotmail.com
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheClear {
    /**
     * 缓存名称，可使用 ognl 表达式，注意：此时表达式的 retVal 属性不可用
     *
     * @return cache name
     */
    String value() default "";

    /**
     * 删除的条件，可使用 ognl 表达式
     *
     * @return 条件表达式
     */
    String condition() default "";

    /**
     * 是否前置删除，即调用目标方法之前就先删除缓存
     *
     * @return 默认 true
     */
    boolean preClear() default true;

    /**
     * 前置删除是否需要延迟，合理的延迟可以避免数据不一致问题
     *
     * @return 默认 0，即时删除
     */
    int preClearTimeout() default 0;
}
