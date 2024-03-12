package com.kfyty.boot.feign.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 标记 feign 接口注解
 *
 * @author kfyty725
 * @date 2024/3/08 18:55
 * @email kfyty725@hotmail.com
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeignClient {
    /**
     * 注册中心服务名称
     *
     * @return service name
     */
    String value() default "";

    /**
     * 使用注册中心时，为基础路径
     * 为使用注册中心时，为 ip+port+基础路径
     *
     * @return host
     */
    String url() default "";
}
