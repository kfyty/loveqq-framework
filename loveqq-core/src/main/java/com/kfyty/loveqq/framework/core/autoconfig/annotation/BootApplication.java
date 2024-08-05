package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 启动类注解
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Documented
@Configuration
@EnableAutoConfiguration
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BootApplication {
    /**
     * bean name
     *
     * @return bean name
     */
    @AliasFor(annotation = Configuration.class)
    String value() default "";

    /**
     * 需要扫描的基础包名
     */
    @AliasFor(annotation = EnableAutoConfiguration.class)
    String[] scan() default {};

    /**
     * 排除 k.factories 中的配置类，被排除的类的一切嵌套配置将不会被加载，前提是没有被自动扫描到，否则无效
     * 如果需要排除非 k.factories 中的类，请使用 {@link ComponentScan#excludeFilter()}
     */
    @AliasFor(annotation = EnableAutoConfiguration.class)
    Class<?>[] exclude() default {};

    /**
     * 作用同 exclude()，需指定配置类的全限定名称
     */
    @AliasFor(annotation = EnableAutoConfiguration.class)
    String[] excludeNames() default {};

    /**
     * 是否要代理类，而不是接口，默认 false
     *
     * @return false if jdk proxy
     */
    boolean proxyTargetClass() default false;
}
