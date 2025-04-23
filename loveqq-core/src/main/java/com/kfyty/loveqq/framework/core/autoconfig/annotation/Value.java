package com.kfyty.loveqq.framework.core.autoconfig.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 描述: 配置字段值
 *
 * @author kfyty725
 * @date 2022/3/12 14:58
 * @email kfyty725@hotmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Value {
    /**
     * 字段的值，根据 {@link #bind()} 不同，取值规则不同
     *
     * <p>
     * 当 {@link this#bind()} == false 时，该值表示字段的值，将自动转换类型赋值给被注解的字段，支持 ${} 从配置文件中取值，
     * 此时仅支持基础数据类型以及逗号分割的集合类型，并且支持 ${key:default} 的形式设置默认值</br>
     * eg: https://{ip}
     * </p>
     * <p>
     * 当 {@link this#bind()} == true 时，该值表示配置前缀，将自动进行配置绑定，仍然支持 ${} 从配置文件中取值，但最终的解析值仍然表示前缀，
     * 此时支持复杂属性绑定，但是将不支持 ${key:default} 的形式设置默认值
     * </p>
     *
     * @see com.kfyty.loveqq.framework.core.utils.ReflectUtil#isBaseDataType(Class)
     */
    String value();

    /**
     * 是否绑定嵌套属性
     *
     * @return true/false
     */
    boolean bind() default false;
}
