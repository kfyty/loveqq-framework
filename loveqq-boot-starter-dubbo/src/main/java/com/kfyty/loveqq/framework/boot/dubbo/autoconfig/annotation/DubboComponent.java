package com.kfyty.loveqq.framework.boot.dubbo.autoconfig.annotation;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.Service;
import com.kfyty.loveqq.framework.core.lang.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: dubbo 组件，该注解标记的组件会进行 {@link org.apache.dubbo.config.annotation.DubboReference} 处理
 *
 * @author kfyty725
 * @date 2021/6/12 12:57
 * @email kfyty725@hotmail.com
 */
@Service
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DubboComponent {
    /**
     * 基础包名
     */
    @AliasFor(annotation = Service.class)
    String value() default "";

    /**
     * @see Service#resolve()
     */
    @AliasFor(annotation = Service.class)
    boolean resolve() default true;
}
