package com.kfyty.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅当处理器方法只有一个对象时，value 可以为空
 * 当参数是基本类型时，value 为参数名称
 * 当参数是集合或者数组时：
 *      若同名参数只有一个，则其值为 json 串
 *      若同名参数有多个，则其值为普通值
 * 当参数是对象时，value 为 前缀 + 对象属性，eg:user.id、dept.id，两个 id 将分别映射到 User 和 Dept
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value() default "";

    String defaultValue() default "";
}
