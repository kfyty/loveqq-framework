package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 用以支持异步任务
 * <b>
 * 仅在类上同时注释时，方法注释才有效
 * </b>
 *
 * @author kfyty725
 * @date 2021/6/26 11:03
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.boot.proxy.AsyncMethodInterceptorProxy
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Async {
    /**
     * 执行该任务的线程池的 bean name，必须是 {@link java.util.concurrent.ExecutorService} 的子类
     */
    String value() default "";

    /**
     * 用于支持异步方法转同步
     * 被注解的方法中，当在该方法体中(该方法的整个调用链路中)调用其他异步方法时，其他异步方法自动转为同步调用，而无需显示手动等待
     * <p>
     * 其他异步方法包含 {@link Async} 注解的方法，以及返回值为 {@link java.util.concurrent.Future}/{@link java.util.concurrent.CompletionStage} 的可代理方法
     * <b>
     * 仅在类上同时注释 {@link Async} 时，方法注释才有效
     * </b>
     * </p>
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Await {
        /**
         * 由于整个调用链路有效，因此如果有某个异步方法必须异步，则可以再次注解该方法，并设置为 false，则可以覆盖之前的设置
         *
         * @return true/false
         */
        boolean value() default true;
    }
}
