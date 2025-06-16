package com.kfyty.loveqq.framework.web.core.annotation.bind;

import com.kfyty.loveqq.framework.core.autoconfig.annotation.ConfigurationProperties;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

/**
 * 当 value 为空时，则需要 -parameters 编译参数支持
 * 该注解绑定逻辑和 {@link ConfigurationProperties} 注解绑定逻辑相同
 * 因此对应的参数传参方式也相同
 *
 * @author kfyty725
 * @date 2021/5/22 14:25
 * @email kfyty725@hotmail.com
 * @see com.kfyty.loveqq.framework.web.core.request.resolver.RequestParamMethodArgumentResolver
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    /**
     * 参数名称，默认取 {@link Parameter#getName()}，需要 -parameters 编译参数支持
     *
     * @return 参数名称
     */
    String value() default "";

    /**
     * 参数值是否必须存在
     * 该值的限制仅限于基础数据类型，对象类型无效
     *
     * @return true/false
     */
    boolean required() default true;

    /**
     * 默认值
     * 该默认值仅适用于基础数据类型，对象类型无效
     *
     * @return 默认值
     */
    String defaultValue() default "";
}
