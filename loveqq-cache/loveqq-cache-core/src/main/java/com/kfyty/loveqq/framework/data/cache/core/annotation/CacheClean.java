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
 * @see com.kfyty.loveqq.framework.data.cache.core.proxy.AbstractCacheInterceptorProxy
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheClean {
    /**
     * 缓存名称，可使用 ${} 占位符引用配置文件的值，可使用 ${ognl} 表示 ognl 表达式，注意：此时表达式的 retVal 属性不可用
     * 内置变量：
     * ioc: {@link com.kfyty.loveqq.framework.core.autoconfig.beans.BeanFactory}
     * this: 当前方法所在实例
     * m: 当前方法对象，{@link java.lang.reflect.Method}
     * args: 当前方法参数数组
     * p0 (p{index}): 取值方法参数对象，{@link java.lang.reflect.Parameter}
     * arg0 (arg{index}): 取值方法参数，也可以直接引用方法参数名称
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
    boolean preClean() default true;

    /**
     * 前置删除是否需要延迟，合理的延迟可以避免数据不一致问题
     * 大于 0 时，即延时双删
     *
     * @return 默认 0，即时删除
     */
    int delay() default 0;
}
